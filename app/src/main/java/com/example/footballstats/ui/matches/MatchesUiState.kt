package com.example.footballstats.ui.matches

import com.example.footballstats.domain.model.FootballMatch

data class MatchesUiState(
    val matches: List<FootballMatch> = emptyList(),
    val yearInput: String = "2023",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)