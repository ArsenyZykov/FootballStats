package com.example.footballstats.domain.model

data class FootballMatch(
    val id: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val dateUtc: Long?,
    val statusName: String,
    val elapsedMinutes: Int?,
    val leagueName: String,
    val countryName: String,
    val seasonYear: Int?,
    val roundName: String?
)