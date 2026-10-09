package com.example.footballstats.data.repository

import com.example.footballstats.data.remote.FootballApi
import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.TeamsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamsRepositoryImpl(
    private val api: FootballApi
) : TeamsRepository {

    override suspend fun getTeams(
        name: String?,
        offset: Int,
        limit: Int
    ): List<Team> = withContext(Dispatchers.IO) {
        val response = api.getTeams(
            name = name?.trim()?.takeIf { it.isNotEmpty() },
            offset = offset,
            limit = limit
        )

        if (!response.status.equals("OK", ignoreCase = true)) {
            throw IllegalStateException("API не смог загрузить команды")
        }

        val teams = response.data
            ?: throw IllegalStateException("API вернул некорректный ответ")

        teams.map { dto ->
            Team(
                id = dto.id,
                name = dto.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Название не указано",
                country = dto.country?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Страна не указана",
                logoUrl = dto.logoUrl
                    ?.takeIf { it.isNotBlank() }
            )
        }
    }
}