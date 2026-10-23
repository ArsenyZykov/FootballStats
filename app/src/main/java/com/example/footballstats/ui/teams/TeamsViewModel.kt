package com.example.footballstats.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.model.TeamFilters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class TeamsViewModel : ViewModel() {

    private val getTeamsUseCase = FootballDependencies.getTeamsUseCase
    private val filtersRepository = FootballDependencies.teamFiltersRepository
    private val badgeCache = FootballDependencies.filtersBadgeCache

    private val _uiState = MutableStateFlow(
        TeamsUiState(isLoading = true)
    )
    val uiState = _uiState.asStateFlow()

    val showFilterBadge = badgeCache.showBadge

    private var appliedFilters = TeamFilters()

    private var loadingJob: Job? = null
    private var filtersJob: Job? = null
    private var filtersLoaded = false

    init {
        observeFilters()
    }

    fun retry() {
        if (filtersLoaded) {
            loadTeams(appliedFilters)
        } else {
            observeFilters()
        }
    }

    fun getTeamById(id: Int): Team? {
        return _uiState.value.teams.find { it.id == id }
    }

    private fun observeFilters() {
        filtersJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        filtersJob = viewModelScope.launch {
            try {
                filtersRepository.filters.collect { filters ->
                    filtersLoaded = true
                    appliedFilters = filters

                    badgeCache.update(filters)

                    _uiState.update {
                        it.copy(searchQuery = filters.name)
                    }

                    loadTeams(filters)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                filtersLoaded = false
                loadingJob?.cancel()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Не удалось прочитать настройки поиска."
                    )
                }
            }
        }
    }

    private fun loadTeams(filters: TeamFilters) {
        loadingJob?.cancel()

        _uiState.update {
            it.copy(
                teams = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        loadingJob = viewModelScope.launch {
            try {
                val teams = getTeamsUseCase(
                    name = filters.name,
                    country = filters.country,
                    sortOrder = filters.sortOrder
                )

                ensureActive()

                _uiState.update {
                    it.copy(
                        teams = teams,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val message = when (exception) {
                    is IOException ->
                        "Не удалось подключиться. Проверьте интернет и повторите."

                    else ->
                        "Не удалось загрузить команды. Попробуйте ещё раз."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            }
        }
    }
}