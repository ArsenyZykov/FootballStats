package com.example.footballstats.data.remote

data class MatchesResponseDto(
    val status: String?,
    val count: Int?,
    val data: List<MatchDto>?,
    val offset: Int?
)

data class MatchDto(
    val id: Int,
    val date: String?,
    val dateUtc: Long?,
    val statusName: String?,
    val elapsed: Int?,
    val homeResult: Int?,
    val awayResult: Int?,
    val homeTeam: MatchTeamDto?,
    val awayTeam: MatchTeamDto?,
    val season: MatchSeasonDto?,
    val roundName: String?
)

data class MatchTeamDto(
    val id: Int,
    val name: String?
)

data class MatchSeasonDto(
    val year: Int?,
    val league: MatchLeagueDto?
)

data class MatchLeagueDto(
    val id: Int,
    val name: String?,
    val country: CountryDto?
)