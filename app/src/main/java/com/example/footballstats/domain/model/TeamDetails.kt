package com.example.footballstats.domain.model

data class TeamDetails(
    val id: Int,
    val name: String,
    val country: String,
    val code: String?,
    val foundedYear: Int?,
    val stadiumName: String?,
    val stadiumCity: String?,
    val stadiumAddress: String?,
    val stadiumCapacity: Int?,
    val coachName: String?,
    val players: List<TeamPlayer>,
    val seasons: List<TeamSeason>
)

data class TeamPlayer(
    val id: Int,
    val name: String
)

data class TeamSeason(
    val leagueId: Int,
    val leagueName: String,
    val year: Int
)