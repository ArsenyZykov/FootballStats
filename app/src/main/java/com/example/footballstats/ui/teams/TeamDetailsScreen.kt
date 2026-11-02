package com.example.footballstats.ui.teams

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.model.Team

@Composable
fun TeamDetailsRoute(onBackClick: () -> Unit) {
    val model: TeamDetailsViewModel = viewModel(factory = FootballDependencies.viewModelFactory)
    val state by model.uiState.collectAsState()
    val context = LocalContext.current
    TeamDetailsScreen(
        uiState = state,
        onToggleFavorite = model::toggleFavorite,
        onRefresh = model::refresh,
        onBackClick = onBackClick,
        onShare = { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться командой"))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    uiState: TeamDetailsUiState,
    onToggleFavorite: () -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onShare: (String) -> Unit
) {
    val content = uiState.content
    if (content == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isLoading) CircularProgressIndicator()
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            uiState.favoriteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (!uiState.isLoading) Button(onClick = onRefresh) { Text("Повторить") }
            TextButton(onClick = onBackClick) { Text("Назад") }
        }
        return
    }
    val team = content.team
    var showAllPlayers by rememberSaveable(team.id) {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Карточка команды",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !uiState.isLoading &&
                                !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить подробности"
                        )
                    }

                    IconButton(
                        onClick = { onShare(content.shareText) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val (logo, name, country, detailsContent) = createRefs()

            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .constrainAs(logo) {
                        top.linkTo(parent.top, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TeamLogo(
                        team = team,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Text(
                text = team.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(name) {
                    top.linkTo(logo.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                text = team.country,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(country) {
                    top.linkTo(name.bottom, margin = 6.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Column(
                modifier = Modifier.constrainAs(detailsContent) {
                    top.linkTo(country.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleFavorite,
                    enabled = uiState.canChangeFavorite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (uiState.isFavorite) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (uiState.isFavorite) {
                            "Убрать из избранного"
                        } else {
                            "В избранное"
                        }
                    )
                }

                uiState.favoriteError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (uiState.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text("Загружаем подробности…")
                    }
                }

                if (uiState.isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !uiState.isLoading &&
                                !uiState.isRefreshing
                    ) {
                        Text("Повторить")
                    }
                }

                content.notice?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                content.sections.forEach { section ->
                    DetailsSection(section.title) {
                        section.fields.forEach { field -> DetailsField(field.label, field.value) }
                    }
                }
                if (content.sourceLabel != null) {
                    DetailsSection("Игроки по данным API") {
                        if (content.players.isEmpty()) Text("Нет данных")
                        val visiblePlayers = if (showAllPlayers) content.players else content.players.take(6)
                        visiblePlayers.forEach { Text(it) }
                        if (content.players.size > 6) {
                            TextButton(onClick = { showAllPlayers = !showAllPlayers }) {
                                Text(if (showAllPlayers) "Свернуть" else content.showAllPlayersLabel)
                            }
                        }
                    }
                    DetailsSection("Турниры и сезоны в базе") {
                        if (content.tournaments.isEmpty()) Text("Нет данных")
                        content.tournaments.forEach { DetailsField(it.label, it.value) }
                    }
                    Text(
                        text = content.sourceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Источник: SStats.net",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun DetailsField(
    label: String,
    value: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value ?: "Нет данных",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamDetailsPreview() {
    MaterialTheme {
        TeamDetailsScreen(
            uiState = TeamDetailsUiState(
                content = TeamDetailsUiModel(
                    team = Team(1, "Arsenal", "England", null),
                    shareText = "Arsenal\nСтрана: England"
                ),
                isLoading = false,
                favoritesLoaded = true
            ),
            onToggleFavorite = {}, onRefresh = {}, onBackClick = {}, onShare = {}
        )
    }
}
