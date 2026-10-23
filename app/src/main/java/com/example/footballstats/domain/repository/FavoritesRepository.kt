package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun observeTeams(): Flow<List<Team>>

    suspend fun setFavorite(
        team: Team,
        isFavorite: Boolean
    )
}