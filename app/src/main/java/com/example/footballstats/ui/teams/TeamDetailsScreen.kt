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
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.TeamDetailsIssue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    team: Team,
    isFavorite: Boolean,
    canChangeFavorite: Boolean,
    favoriteError: String?,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val detailsViewModel: TeamDetailsViewModel = viewModel()
    val uiState by detailsViewModel.uiState.collectAsState()

    val result = uiState.result
    val details = result?.details

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
                        onClick = detailsViewModel::refresh,
                        enabled = !uiState.isLoading &&
                                !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить подробности"
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine(details?.name ?: team.name)
                                appendLine(
                                    "Страна: ${details?.country ?: team.country}"
                                )

                                details?.code?.let {
                                    appendLine("Код: $it")
                                }

                                details?.foundedYear?.let {
                                    appendLine("Год основания: $it")
                                }

                                details?.stadiumName?.let {
                                    appendLine("Стадион: $it")
                                }

                                details?.stadiumCity?.let {
                                    appendLine("Город: $it")
                                }

                                appendLine("Источник: SStats.net")
                            }.trim()

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Поделиться командой"
                                )
                            )
                        }
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
            val (logo, name, country, content) = createRefs()

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
                text = details?.name ?: team.name,
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
                text = details?.country ?: team.country,
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
                modifier = Modifier.constrainAs(content) {
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
                    enabled = canChangeFavorite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (isFavorite) {
                            "Убрать из избранного"
                        } else {
                            "В избранное"
                        }
                    )
                }

                favoriteError?.let {
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
                        onClick = detailsViewModel::refresh,
                        enabled = !uiState.isLoading &&
                                !uiState.isRefreshing
                    ) {
                        Text("Повторить")
                    }
                }

                if (details != null && result != null) {
                    val notice = when (result.issue) {
                        TeamDetailsIssue.REFRESH_FAILED ->
                            "Не удалось обновить данные. " +
                                    "Показана сохранённая копия."

                        TeamDetailsIssue.CACHE_WRITE_FAILED ->
                            "Данные загружены, но не удалось сохранить " +
                                    "их для просмотра без интернета."

                        null -> null
                    }

                    notice?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    DetailsSection(title = "О команде") {
                        DetailsField(
                            label = "Код команды",
                            value = details.code
                        )

                        DetailsField(
                            label = "Год основания",
                            value = details.foundedYear?.toString()
                        )
                    }

                    DetailsSection(title = "Стадион") {
                        DetailsField(
                            label = "Название",
                            value = details.stadiumName
                        )

                        DetailsField(
                            label = "Город",
                            value = details.stadiumCity
                        )

                        DetailsField(
                            label = "Адрес",
                            value = details.stadiumAddress
                        )

                        DetailsField(
                            label = "Вместимость",
                            value = details.stadiumCapacity?.let {
                                NumberFormat.getIntegerInstance(
                                    Locale.getDefault()
                                ).format(it)
                            }
                        )
                    }

                    DetailsSection(title = "Тренер по данным API") {
                        Text(
                            text = details.coachName ?: "Нет данных"
                        )
                    }

                    DetailsSection(title = "Игроки по данным API") {
                        if (details.players.isEmpty()) {
                            Text("Нет данных")
                        } else {
                            val visiblePlayers = if (showAllPlayers) {
                                details.players
                            } else {
                                details.players.take(6)
                            }

                            visiblePlayers.forEach { player ->
                                Text(text = "• ${player.name}")
                            }

                            if (details.players.size > 6) {
                                TextButton(
                                    onClick = {
                                        showAllPlayers = !showAllPlayers
                                    }
                                ) {
                                    Text(
                                        text = if (showAllPlayers) {
                                            "Свернуть"
                                        } else {
                                            "Показать всех (${details.players.size})"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    DetailsSection(title = "Турниры и сезоны в базе") {
                        if (details.seasons.isEmpty()) {
                            Text("Нет данных")
                        } else {
                            val tournaments = details.seasons
                                .groupBy { it.leagueId }
                                .values
                                .sortedBy { it.first().leagueName }

                            tournaments.forEach { seasons ->
                                Text(
                                    text = seasons.first().leagueName,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Годы сезонов: " +
                                            seasons
                                                .map { it.year }
                                                .distinct()
                                                .sortedDescending()
                                                .joinToString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = (
                                if (result.isFromCache) {
                                    "Сохранённая копия"
                                } else {
                                    "Загружено из SStats.net"
                                }
                                ) + " · " + formatDetailsDate(result.updatedAt),
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

private fun formatDetailsDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd.MM.yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp))
}