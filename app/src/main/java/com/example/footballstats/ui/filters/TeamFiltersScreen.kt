package com.example.footballstats.ui.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.model.TeamSortOrder

private val countryOptions = listOf(
    "" to "Все страны",
    "England" to "Англия",
    "Spain" to "Испания",
    "Germany" to "Германия",
    "Italy" to "Италия",
    "France" to "Франция",
    "Russia" to "Россия",
    "Portugal" to "Португалия",
    "Netherlands" to "Нидерланды",
    "Brazil" to "Бразилия",
    "Argentina" to "Аргентина",
    "Croatia" to "Хорватия",
    "Belgium" to "Бельгия"
)

private val sortOptions = listOf(
    TeamSortOrder.DEFAULT to "Порядок API",
    TeamSortOrder.NAME_ASC to "Название: А–Я",
    TeamSortOrder.NAME_DESC to "Название: Я–А"
)

@Composable
fun TeamFiltersRoute(onBackClick: () -> Unit, onFiltersSaved: () -> Unit) {
    val model: TeamFiltersViewModel = viewModel(factory = FootballDependencies.viewModelFactory)
    val state by model.uiState.collectAsState()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onFiltersSaved()
    }
    TeamFiltersScreen(
        uiState = state,
        onBackClick = onBackClick,
        onNameChange = model::onNameChange,
        onCountryChange = model::onCountryChange,
        onSortOrderChange = model::onSortOrderChange,
        onReset = model::resetDraft,
        onSave = model::saveFilters,
        onRetry = model::loadFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamFiltersScreen(
    uiState: TeamFiltersUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onSortOrderChange: (TeamSortOrder) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки поиска",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isReady) {
                    val canEdit = !uiState.isSaving && !uiState.isSaved

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Название команды") },
                        placeholder = { Text("Например, Arsenal") },
                        singleLine = true,
                        enabled = canEdit
                    )

                    FilterDropdown(
                        title = "Страна",
                        selectedText = countryOptions
                            .firstOrNull { it.first == uiState.country }
                            ?.second ?: uiState.country,
                        options = countryOptions,
                        enabled = canEdit,
                        onSelect = onCountryChange
                    )

                    FilterDropdown(
                        title = "Сортировка",
                        selectedText = sortOptions
                            .first { it.first == uiState.sortOrder }
                            .second,
                        options = sortOptions,
                        enabled = canEdit,
                        onSelect = onSortOrderChange
                    )

                    Text(
                        text = "Сортировка применяется к загруженной " +
                                "странице команд. Все настройки сохраняются " +
                                "по кнопке «Готово».",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = onReset,
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сбросить всё")
                    }

                    Button(
                        onClick = onSave,
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(Modifier.width(10.dp))
                        }

                        Text(
                            if (uiState.isSaving) {
                                "Сохраняем…"
                            } else {
                                "Готово"
                            }
                        )
                    }
                }

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!uiState.isReady) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Повторить чтение настроек")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> FilterDropdown(
    title: String,
    selectedText: String,
    options: List<Pair<T, String>>,
    enabled: Boolean,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedText)
            }

            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                options.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            expanded = false
                            onSelect(value)
                        }
                    )
                }
            }
        }
    }
}