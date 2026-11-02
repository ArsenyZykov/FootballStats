package com.example.footballstats.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.model.TeamFilters
import com.example.footballstats.domain.model.TeamSortOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamFiltersUiState(
    val name: String = "",
    val country: String = "",
    val sortOrder: TeamSortOrder = TeamSortOrder.DEFAULT,
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class TeamFiltersViewModel : ViewModel() {

    private val repository = FootballDependencies.teamFiltersRepository
    private val badgeCache = FootballDependencies.filtersBadgeCache

    private val _uiState = MutableStateFlow(TeamFiltersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFilters()
    }

    fun loadFilters() {
        _uiState.value = TeamFiltersUiState()

        viewModelScope.launch {
            try {
                val filters = repository.filters.first()

                badgeCache.update(filters)

                _uiState.value = TeamFiltersUiState(
                    name = filters.name,
                    country = filters.country,
                    sortOrder = filters.sortOrder,
                    isLoading = false,
                    isReady = true
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = TeamFiltersUiState(
                    isLoading = false,
                    errorMessage = "Не удалось прочитать настройки."
                )
            }
        }
    }

    fun onNameChange(name: String) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(name = name, errorMessage = null)
        }
    }

    fun onCountryChange(country: String) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(country = country, errorMessage = null)
        }
    }

    fun onSortOrderChange(sortOrder: TeamSortOrder) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(sortOrder = sortOrder, errorMessage = null)
        }
    }

    fun resetDraft() {
        if (!canEdit()) return

        _uiState.update {
            it.copy(
                name = "",
                country = "",
                sortOrder = TeamSortOrder.DEFAULT,
                errorMessage = null
            )
        }
    }

    fun saveFilters() {
        if (!canEdit()) return

        val state = _uiState.value

        val filters = TeamFilters(
            name = state.name.trim(),
            country = state.country,
            sortOrder = state.sortOrder
        )

        _uiState.update {
            it.copy(isSaving = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                repository.saveFilters(filters)
                badgeCache.update(filters)

                _uiState.update {
                    it.copy(
                        name = filters.name,
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить настройки."
                    )
                }
            }
        }
    }

    private fun canEdit(): Boolean {
        val state = _uiState.value

        return state.isReady && !state.isSaving && !state.isSaved
    }
}