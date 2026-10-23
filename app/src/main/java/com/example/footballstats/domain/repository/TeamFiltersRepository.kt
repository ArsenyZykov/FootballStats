package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.TeamFilters
import kotlinx.coroutines.flow.Flow

interface TeamFiltersRepository {

    val filters: Flow<TeamFilters>

    suspend fun saveFilters(filters: TeamFilters)
}