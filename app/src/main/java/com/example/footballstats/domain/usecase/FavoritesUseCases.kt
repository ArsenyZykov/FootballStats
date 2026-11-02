package com.example.footballstats.domain.usecase

import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteTeamsUseCase(
    private val repository: FavoritesRepository
) {

    operator fun invoke(): Flow<List<Team>> {
        return repository.observeTeams()
    }
}

class SetFavoriteTeamUseCase(
    private val repository: FavoritesRepository
) {

    suspend operator fun invoke(
        team: Team,
        isFavorite: Boolean
    ) {
        repository.setFavorite(
            team = team,
            isFavorite = isFavorite
        )
    }
}