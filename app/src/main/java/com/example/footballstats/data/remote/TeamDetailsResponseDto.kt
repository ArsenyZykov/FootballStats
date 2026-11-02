package com.example.footballstats.data.remote

data class TeamDetailsResponseDto(
    val status: String?,
    val data: TeamDetailsDto?
)

data class TeamDetailsDto(
    val id: Int?,
    val name: String?,
    val country: CountryDto?,
    val code: String?,
    val founded: Int?,
    val venue: TeamVenueDto?,
    val coach: TeamCoachDto?,
    val players: List<TeamPlayerDto>?,
    val seasons: List<TeamSeasonDto>?
)

data class TeamVenueDto(
    val name: String?,
    val address: String?,
    val city: String?,
    val capacity: Int?
)

data class TeamCoachDto(
    val id: Int?,
    val name: String?
)

data class TeamPlayerDto(
    val id: Int?,
    val name: String?
)

data class TeamSeasonDto(
    val uid: String?,
    val year: Int?,
    val league: TeamLeagueDto?
)

data class TeamLeagueDto(
    val id: Int?,
    val name: String?,
    val country: CountryDto?
)