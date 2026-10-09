package com.example.footballstats.data.repository

import com.example.footballstats.data.remote.FootballApi
import com.example.footballstats.domain.model.FootballMatch
import com.example.footballstats.domain.repository.MatchesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatchesRepositoryImpl(
    private val api: FootballApi
) : MatchesRepository {

    override suspend fun getMatches(
        leagueId: Int,
        year: Int,
        offset: Int,
        limit: Int
    ): List<FootballMatch> = withContext(Dispatchers.IO) {
        val response = api.getMatches(
            leagueId = leagueId,
            year = year,
            offset = offset,
            limit = limit
        )

        if (!response.status.equals("OK", ignoreCase = true)) {
            throw IllegalStateException("API не смог загрузить матчи")
        }

        val matches = response.data
            ?: throw IllegalStateException("API вернул некорректный ответ")

        matches.map { dto ->
            FootballMatch(
                id = dto.id,
                homeTeamName = dto.homeTeam?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Хозяева",
                awayTeamName = dto.awayTeam?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Гости",
                homeScore = dto.homeResult,
                awayScore = dto.awayResult,
                dateUtc = dto.dateUtc,
                statusName = dto.statusName
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown",
                elapsedMinutes = dto.elapsed,
                leagueName = dto.season?.league?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Лига не указана",
                countryName = dto.season?.league?.country?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Страна не указана",
                seasonYear = dto.season?.year,
                roundName = dto.roundName
                    ?.takeIf { it.isNotBlank() }
            )
        }
    }
}