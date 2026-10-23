package com.example.footballstats.data.repository

import com.example.footballstats.data.local.TeamDetailsDao
import com.example.footballstats.data.local.TeamDetailsEntity
import com.example.footballstats.data.remote.FootballApi
import com.example.footballstats.data.remote.TeamDetailsDto
import com.example.footballstats.domain.model.TeamDetails
import com.example.footballstats.domain.model.TeamPlayer
import com.example.footballstats.domain.model.TeamSeason
import com.example.footballstats.domain.repository.TeamDetailsIssue
import com.example.footballstats.domain.repository.TeamDetailsRepository
import com.example.footballstats.domain.repository.TeamDetailsResult
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamDetailsRepositoryImpl(
    private val api: FootballApi,
    private val dao: TeamDetailsDao
) : TeamDetailsRepository {

    private val gson = Gson()

    override suspend fun getDetails(
        teamId: Int,
        refresh: Boolean
    ): TeamDetailsResult = withContext(Dispatchers.IO) {

        val cached = readCachedDetails(teamId)

        if (cached != null && !refresh) {
            return@withContext cached
        }

        val dto: TeamDetailsDto
        val details: TeamDetails

        try {
            val response = api.getTeamDetails(teamId)

            check(response.status.equals("OK", ignoreCase = true)) {
                "API не смог загрузить подробности команды"
            }

            dto = checkNotNull(response.data) {
                "API вернул пустую карточку команды"
            }

            details = dto.toDomain(expectedId = teamId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (cached != null) {
                return@withContext cached.copy(
                    issue = TeamDetailsIssue.REFRESH_FAILED
                )
            }

            throw exception
        }

        val updatedAt = System.currentTimeMillis()

        val cacheIssue = try {
            dao.insert(
                TeamDetailsEntity(
                    teamId = teamId,
                    payload = gson.toJson(dto),
                    savedAt = updatedAt
                )
            )

            null
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            TeamDetailsIssue.CACHE_WRITE_FAILED
        }

        TeamDetailsResult(
            details = details,
            updatedAt = updatedAt,
            isFromCache = false,
            issue = cacheIssue
        )
    }

    private suspend fun readCachedDetails(
        teamId: Int
    ): TeamDetailsResult? {
        return try {
            val entity = dao.getById(teamId) ?: return null

            val dto = gson.fromJson(
                entity.payload,
                TeamDetailsDto::class.java
            ) ?: return null

            TeamDetailsResult(
                details = dto.toDomain(expectedId = teamId),
                updatedAt = entity.savedAt,
                isFromCache = true
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Если сохранённая копия не читается,
            // попробуем получить её заново из API.
            null
        }
    }

    private fun TeamDetailsDto.toDomain(
        expectedId: Int
    ): TeamDetails {
        check(id == expectedId) {
            "ID команды в ответе не совпадает с запросом"
        }

        val mappedPlayers = players.orEmpty()
            .mapNotNull { player ->
                val playerId = player.id ?: return@mapNotNull null
                val playerName = player.name.clean()
                    ?: return@mapNotNull null

                if (playerId <= 0) {
                    return@mapNotNull null
                }

                TeamPlayer(
                    id = playerId,
                    name = playerName
                )
            }
            .distinctBy { it.id }
            .sortedBy { it.name }

        val mappedSeasons = seasons.orEmpty()
            .mapNotNull { season ->
                val leagueId = season.league?.id
                    ?: return@mapNotNull null

                val leagueName = season.league.name.clean()
                    ?: return@mapNotNull null

                val year = season.year
                    ?: return@mapNotNull null

                if (leagueId <= 0 || year <= 0) {
                    return@mapNotNull null
                }

                TeamSeason(
                    leagueId = leagueId,
                    leagueName = leagueName,
                    year = year
                )
            }
            .distinctBy { it.leagueId to it.year }
            .sortedWith(
                compareByDescending<TeamSeason> { it.year }
                    .thenBy { it.leagueName }
            )

        return TeamDetails(
            id = expectedId,
            name = name.clean() ?: "Название не указано",
            country = country?.name.clean() ?: "Страна не указана",
            code = code.clean(),
            foundedYear = founded?.takeIf { it > 0 },
            stadiumName = venue?.name.clean(),
            stadiumCity = venue?.city.clean(),
            stadiumAddress = venue?.address.clean(),
            stadiumCapacity = venue?.capacity?.takeIf { it > 0 },
            coachName = coach?.name.clean(),
            players = mappedPlayers,
            seasons = mappedSeasons
        )
    }

    private fun String?.clean(): String? {
        return this?.trim()?.takeIf { it.isNotEmpty() }
    }
}