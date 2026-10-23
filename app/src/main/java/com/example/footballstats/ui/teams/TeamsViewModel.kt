package com.example.footballstats.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.di.FootballDependencies
import com.example.footballstats.domain.model.Team
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class TeamsViewModel : ViewModel() {

    private val getTeamsUseCase = FootballDependencies.getTeamsUseCase

    private val _uiState = MutableStateFlow(TeamsUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    init {
        loadTeams()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }

    fun searchTeams() {
        loadTeams()
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(searchQuery = "")
        }
        loadTeams()
    }

    fun retry() {
        loadTeams()
    }

    fun getTeamById(id: Int): Team? {
        return _uiState.value.teams.find { it.id == id }
    }

    private fun loadTeams() {
        loadingJob?.cancel()

        val query = _uiState.value.searchQuery.trim()

        _uiState.update {
            it.copy(
                teams = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        loadingJob = viewModelScope.launch {
            try {
                val teams = getTeamsUseCase(name = query)

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