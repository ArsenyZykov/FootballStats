package com.example.footballstats.ui.teams

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.footballstats.R
import com.example.footballstats.domain.model.Team
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(
    uiState: TeamsUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onRetry: () -> Unit,
    onTeamClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Футбольные команды",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название команды") },
                placeholder = { Text("Например, Arsenal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        TextButton(onClick = onClearSearch) {
                            Text("Сброс")
                        }
                    }
                }
            )

            Button(
                onClick = onSearch,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Text("Найти")
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Загружаем команды…")
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onRetry) {
                            Text("Повторить")
                        }
                    }
                }

                uiState.teams.isEmpty() -> {
                    Text(
                        text = "Команды не найдены. Измените запрос.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.teams,
                            key = { it.id }
                        ) { team ->
                            TeamCard(
                                team = team,
                                onClick = { onTeamClick(team.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamLogo(
                team = team,
                modifier = Modifier.size(54.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = team.country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun TeamLogo(
    team: Team,
    modifier: Modifier = Modifier
) {
    val club = team.name.lowercase(Locale.ROOT)
    val country = team.country.lowercase(Locale.ROOT)

    val logoRes = when (country to club) {
        "england" to "arsenal" -> R.drawable.arsenal
        "spain" to "barcelona" -> R.drawable.barcelona
        "germany" to "bayern munich" -> R.drawable.bayern_munich
        "italy" to "inter milan" -> R.drawable.inter_milan
        "france" to "paris saint-germain" -> R.drawable.psg
        "england" to "liverpool" -> R.drawable.liverpool
        "spain" to "real madrid" -> R.drawable.real_madrid
        "germany" to "borussia dortmund" -> R.drawable.borussia_dortmund
        "italy" to "juventus" -> R.drawable.juventus
        "england" to "manchester city" -> R.drawable.manchester_city
        else -> null
    }

    if (logoRes != null) {
        Image(
            painter = painterResource(logoRes),
            contentDescription = "Эмблема ${team.name}",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(
            imageVector = Icons.Default.SportsSoccer,
            contentDescription = "Футбольная команда",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}