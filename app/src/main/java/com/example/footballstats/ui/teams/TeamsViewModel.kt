package com.example.footballstats.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.TeamFilters
import com.example.footballstats.domain.repository.TeamFiltersRepository
import com.example.footballstats.domain.usecase.GetTeamsUseCase
import com.example.footballstats.ui.common.FiltersBadgeCache
import com.example.footballstats.ui.common.TeamSummaryCache
import com.example.footballstats.ui.common.launchWithErrorHandler
import com.example.footballstats.ui.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update

class TeamsViewModel(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val filtersRepository: TeamFiltersRepository,
    private val badgeCache: FiltersBadgeCache,
    private val summaryCache: TeamSummaryCache
) : ViewModel() {

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

    private fun observeFilters() {
        filtersJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        filtersJob = viewModelScope.launchWithErrorHandler(
            onError = {
                filtersLoaded = false
                loadingJob?.cancel()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Не удалось прочитать настройки поиска."
                    )
                }
            }
        ) {
            filtersRepository.filters.collect { filters ->
                filtersLoaded = true
                appliedFilters = filters

                badgeCache.update(filters)

                _uiState.update {
                    it.copy(searchQuery = filters.name)
                }

                loadTeams(filters)
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

        loadingJob = viewModelScope.launchWithErrorHandler(
            onError = { exception ->
                val message = exception.toUserMessage("Не удалось загрузить команды. Попробуйте ещё раз.")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            }
        ) {
            val teams = getTeamsUseCase(
                name = filters.name,
                country = filters.country,
                sortOrder = filters.sortOrder
            )

            ensureActive()
            summaryCache.putAll(teams)

            _uiState.update {
                it.copy(
                    teams = teams,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }
}
