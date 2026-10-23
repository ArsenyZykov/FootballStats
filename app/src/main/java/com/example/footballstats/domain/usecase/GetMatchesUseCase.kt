package com.example.footballstats.domain.usecase

import com.example.footballstats.domain.model.FootballMatch
import com.example.footballstats.domain.repository.MatchesRepository

class GetMatchesUseCase(
    private val repository: MatchesRepository
) {

    suspend operator fun invoke(
        leagueId: Int,
        year: Int,
        offset: Int = 0,
        limit: Int = 20
    ): List<FootballMatch> {
        require(leagueId > 0) {
            "ID лиги должен быть больше нуля"
        }
        require(year > 0) {
            "Год сезона должен быть больше нуля"
        }
        require(offset >= 0) {
            "Смещение не может быть отрицательным"
        }
        require(limit > 0) {
            "Количество матчей должно быть больше нуля"
        }

        return repository.getMatches(
            leagueId = leagueId,
            year = year,
            offset = offset,
            limit = limit
        )
    }
}