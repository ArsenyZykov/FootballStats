package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.TeamDetails

enum class TeamDetailsIssue {
    REFRESH_FAILED,
    CACHE_WRITE_FAILED
}

data class TeamDetailsResult(
    val details: TeamDetails,
    val updatedAt: Long,
    val isFromCache: Boolean,
    val issue: TeamDetailsIssue? = null
)

interface TeamDetailsRepository {

    suspend fun getDetails(
        teamId: Int,
        refresh: Boolean = false
    ): TeamDetailsResult
}