package com.example.footballstats.ui.teams

import com.example.footballstats.domain.model.Team

data class TeamsUiState(
    val teams: List<Team> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)