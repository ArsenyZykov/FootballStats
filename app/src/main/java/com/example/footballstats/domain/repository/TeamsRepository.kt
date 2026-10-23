package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.Team

interface TeamsRepository {

    suspend fun getTeams(
        name: String? = null,
        offset: Int = 0,
        limit: Int = 20
    ): List<Team>
}