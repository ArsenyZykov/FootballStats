package com.example.footballstats.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.di.FootballDependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar

class MatchesViewModel : ViewModel() {

    private val getMatchesUseCase = FootballDependencies.getMatchesUseCase

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    init {
        loadMatches()
    }

    fun onYearChange(value: String) {
        _uiState.update {
            it.copy(
                yearInput = value.filter { char ->
                    char in '0'..'9'
                }.take(4)
            )
        }
    }

    fun loadMatches() {
        loadingJob?.cancel()

        val year = _uiState.value.yearInput.toIntOrNull()
        val maximumYear = Calendar.getInstance().get(Calendar.YEAR) + 1

        if (year == null || year !in 1900..maximumYear) {
            _uiState.update {
                it.copy(
                    matches = emptyList(),
                    isLoading = false,
                    errorMessage = "Введите корректный год сезона."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                matches = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        loadingJob = viewModelScope.launch {
            try {
                val matches = getMatchesUseCase(
                    leagueId = 41,
                    year = year,
                    limit = 20
                )

                ensureActive()

                _uiState.update {
                    it.copy(
                        matches = matches,
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
                        "Не удалось загрузить матчи. Попробуйте ещё раз."
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