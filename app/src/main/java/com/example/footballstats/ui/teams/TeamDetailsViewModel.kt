package com.example.footballstats.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.repository.TeamDetailsResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class TeamDetailsUiState(
    val result: TeamDetailsResult? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class TeamDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val teamId = savedStateHandle.get<Int>("teamId")
    private val getTeamDetails =
        FootballDependencies.getTeamDetailsUseCase

    private val _uiState = MutableStateFlow(TeamDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    init {
        load(refresh = false)
    }

    fun refresh() {
        load(refresh = true)
    }

    private fun load(refresh: Boolean) {
        loadingJob?.cancel()

        val id = teamId

        if (id == null || id <= 0) {
            _uiState.value = TeamDetailsUiState(
                isLoading = false,
                errorMessage = "Не указан ID команды."
            )
            return
        }

        val hasDetails = _uiState.value.result != null

        _uiState.update {
            it.copy(
                isLoading = !hasDetails,
                isRefreshing = hasDetails,
                errorMessage = null
            )
        }

        loadingJob = viewModelScope.launch {
            try {
                val result = getTeamDetails(
                    teamId = id,
                    refresh = refresh
                )

                ensureActive()

                _uiState.value = TeamDetailsUiState(
                    result = result,
                    isLoading = false
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val message = when (exception) {
                    is IOException ->
                        "Нет связи с сервером. Если подробности ещё не " +
                                "сохранены, подключите интернет и повторите."

                    else ->
                        "Не удалось загрузить подробности команды."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = message
                    )
                }
            }
        }
    }
}