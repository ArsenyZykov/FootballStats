package com.example.footballstats.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.usecase.GetFavoriteTeamsUseCase
import com.example.footballstats.domain.usecase.SetFavoriteTeamUseCase
import com.example.footballstats.ui.common.launchWithErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update

data class FavoritesUiState(
    val teams: List<Team> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoaded: Boolean = false,
    val pendingTeamIds: Set<Int> = emptySet(),
    val errorMessage: String? = null
)

class FavoritesViewModel(
    private val getFavoriteTeams: GetFavoriteTeamsUseCase,
    private val setFavoriteTeam: SetFavoriteTeamUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()

    private var observingJob: Job? = null

    init {
        observeFavorites()
    }

    fun retry() {
        observeFavorites()
    }

    fun setFavorite(team: Team, isFavorite: Boolean) {
        val state = _uiState.value

        if (!state.hasLoaded || team.id in state.pendingTeamIds) {
            return
        }

        _uiState.update {
            it.copy(
                pendingTeamIds = it.pendingTeamIds + team.id,
                errorMessage = null
            )
        }

        viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        errorMessage = "Не удалось изменить избранное. Повторите."
                    )
                }
            }
        ) {
            try {
                setFavoriteTeam(
                    team = team,
                    isFavorite = isFavorite
                )
            } finally {
                _uiState.update {
                    it.copy(
                        pendingTeamIds = it.pendingTeamIds - team.id
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        observingJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                hasLoaded = false,
                errorMessage = null
            )
        }

        observingJob = viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLoaded = false,
                        errorMessage = "Не удалось прочитать избранное."
                    )
                }
            }
        ) {
            getFavoriteTeams().collect { teams ->
                _uiState.update {
                    it.copy(
                        teams = teams,
                        isLoading = false,
                        hasLoaded = true
                    )
                }
            }
        }
    }
}
