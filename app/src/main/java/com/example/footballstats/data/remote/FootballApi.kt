package com.example.footballstats.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FootballApi {

    @GET("teams/list")
    suspend fun getTeams(
        @Query("Name") name: String? = null,
        @Query("Offset") offset: Int = 0,
        @Query("Limit") limit: Int = 20,
        @Query("Country") country: String? = null
    ): TeamsResponseDto

    @GET("teams/{id}")
    suspend fun getTeamDetails(
        @Path("id") teamId: Int
    ): TeamDetailsResponseDto

    @GET("games/list")
    suspend fun getMatches(
        @Query("LeagueId") leagueId: Int,
        @Query("Year") year: Int,
        @Query("Offset") offset: Int = 0,
        @Query("Limit") limit: Int = 20
    ): MatchesResponseDto
}