package com.example.footballstats.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.TeamDetailsResult
import com.example.footballstats.domain.usecase.GetFavoriteTeamsUseCase
import com.example.footballstats.domain.usecase.GetTeamDetailsUseCase
import com.example.footballstats.domain.usecase.SetFavoriteTeamUseCase
import com.example.footballstats.ui.common.TeamSummaryCache
import com.example.footballstats.ui.common.launchWithErrorHandler
import com.example.footballstats.ui.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TeamDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getTeamDetails: GetTeamDetailsUseCase,
    private val getFavoriteTeams: GetFavoriteTeamsUseCase,
    private val setFavoriteTeam: SetFavoriteTeamUseCase,
    summaryCache: TeamSummaryCache
) : ViewModel() {
    private val teamId = savedStateHandle.get<Int>("teamId")
    private var team: Team? = teamId?.let(summaryCache::get)
    private var result: TeamDetailsResult? = null
    private val _uiState = MutableStateFlow(TeamDetailsUiState(
        content = team?.let { teamDetailsUiModel(it, null) }
    ))
    val uiState = _uiState.asStateFlow()
    private var loadingJob: Job? = null
    private var favoritesJob: Job? = null

    init {
        if (teamId != null && teamId > 0) observeFavorites()
        load(refresh = false)
    }

    fun refresh() {
        if (!_uiState.value.favoritesLoaded && teamId != null && teamId > 0) observeFavorites()
        load(refresh = true)
    }

    private fun observeFavorites() {
        favoritesJob?.cancel()
        _uiState.update { it.copy(favoriteError = null) }
        favoritesJob = viewModelScope.launchWithErrorHandler(onError = {
            _uiState.update { it.copy(favoritesLoaded = false, favoriteError = "Не удалось прочитать избранное. Нажмите обновить, чтобы повторить.") }
        }) {
            getFavoriteTeams().collect { favorites ->
                val favorite = favorites.find { it.id == teamId }
                if (favorite != null) team = favorite
                _uiState.update {
                    it.copy(
                        content = team?.let { summary -> teamDetailsUiModel(summary, result) },
                        isFavorite = favorite != null,
                        favoritesLoaded = true
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        if (!state.canChangeFavorite) return
        val selectedTeam = state.content?.team ?: return
        _uiState.update { it.copy(isSavingFavorite = true, favoriteError = null) }
        viewModelScope.launchWithErrorHandler(onError = {
            _uiState.update { it.copy(favoriteError = "Не удалось изменить избранное. Повторите.") }
        }) {
            try {
                setFavoriteTeam(selectedTeam, !state.isFavorite)
            } finally {
                _uiState.update { it.copy(isSavingFavorite = false) }
            }
        }
    }

    private fun load(refresh: Boolean) {
        loadingJob?.cancel()
        val id = teamId
        if (id == null || id <= 0) {
            _uiState.value = TeamDetailsUiState(isLoading = false, errorMessage = "Не указан ID команды.")
            return
        }
        _uiState.update {
            it.copy(isLoading = result == null, isRefreshing = result != null, errorMessage = null)
        }
        loadingJob = viewModelScope.launchWithErrorHandler(onError = { error ->
            _uiState.update {
                it.copy(isLoading = false, isRefreshing = false,
                    errorMessage = error.toUserMessage("Не удалось загрузить подробности команды."))
            }
        }) {
            val loaded = getTeamDetails(id, refresh)
            ensureActive()
            result = loaded
            val summary = team ?: Team(id, loaded.details.name, loaded.details.country, null)
            team = summary
            _uiState.update {
                it.copy(content = teamDetailsUiModel(summary, loaded), isLoading = false, isRefreshing = false)
            }
        }
    }
}
