package com.example.footballstats.data.repository

import com.example.footballstats.data.local.FavoriteTeamEntity
import com.example.footballstats.data.local.FavoriteTeamsDao
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTeamsDao
) : FavoritesRepository {

    override fun observeTeams(): Flow<List<Team>> {
        return dao.observeTeams()
            .map { entities ->
                entities.map { entity ->
                    Team(
                        id = entity.id,
                        name = entity.name,
                        country = entity.country,
                        logoUrl = entity.logoUrl
                    )
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun setFavorite(
        team: Team,
        isFavorite: Boolean
    ) {
        if (isFavorite) {
            dao.insert(
                FavoriteTeamEntity(
                    id = team.id,
                    name = team.name,
                    country = team.country,
                    logoUrl = team.logoUrl
                )
            )
        } else {
            dao.deleteById(team.id)
        }
    }
}