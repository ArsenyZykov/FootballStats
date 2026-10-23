package com.example.footballstats.ui.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.domain.model.FootballMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen() {
    val matchesViewModel: MatchesViewModel = viewModel()
    val uiState by matchesViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Футбольные матчи",
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
            Text(
                text = "League One · Англия",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.yearInput,
                onValueChange = matchesViewModel::onYearChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Год начала сезона") },
                supportingText = {
                    Text("Например, 2023 для сезона 2023/24")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { matchesViewModel.loadMatches() }
                )
            )

            Button(
                onClick = matchesViewModel::loadMatches,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Text("Загрузить матчи")
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
                            Text("Загружаем матчи…")
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
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = matchesViewModel::loadMatches
                        ) {
                            Text("Повторить")
                        }
                    }
                }

                uiState.matches.isEmpty() -> {
                    Text(
                        text = "Для выбранного сезона матчи не найдены.",
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
                        item {
                            Text(
                                text = "Показаны первые ${uiState.matches.size} " +
                                        "матчей. Время — по часовому поясу телефона.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(
                            items = uiState.matches,
                            key = { it.id }
                        ) { match ->
                            MatchCard(match = match)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(match: FootballMatch) {
    val score = if (
        match.homeScore != null && match.awayScore != null
    ) {
        "${match.homeScore} : ${match.awayScore}"
    } else {
        "— : —"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = match.leagueName +
                        (match.seasonYear?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = formatMatchDate(match.dateUtc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = match.homeTeamName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = score,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 10.dp
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = match.awayTeamName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider()

            Text(
                text = translateMatchStatus(match.statusName),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            match.roundName?.let { round ->
                Text(
                    text = round,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatMatchDate(timestamp: Long?): String {
    if (timestamp == null) {
        return "Дата не указана"
    }

    val formatter = SimpleDateFormat(
        "dd.MM.yyyy · HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp * 1000L))
}

private fun translateMatchStatus(status: String): String {
    return when (status.lowercase(Locale.ROOT)) {
        "finished" -> "Матч завершён"
        "not started", "scheduled" -> "Матч ещё не начался"
        "first half" -> "Первый тайм"
        "second half" -> "Второй тайм"
        "halftime", "half time" -> "Перерыв"
        "postponed" -> "Матч перенесён"
        "cancelled", "canceled" -> "Матч отменён"
        "unknown" -> "Статус не указан"
        else -> status
    }
}