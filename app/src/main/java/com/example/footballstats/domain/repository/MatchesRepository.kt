package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.FootballMatch

interface MatchesRepository {

    suspend fun getMatches(
        leagueId: Int,
        year: Int,
        offset: Int = 0,
        limit: Int = 20
    ): List<FootballMatch>
}