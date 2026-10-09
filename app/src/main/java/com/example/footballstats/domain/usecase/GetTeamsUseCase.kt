package com.example.footballstats.domain.usecase

import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.TeamsRepository

class GetTeamsUseCase(
    private val repository: TeamsRepository
) {

    suspend operator fun invoke(
        name: String = "",
        offset: Int = 0,
        limit: Int = 20
    ): List<Team> {
        require(offset >= 0) {
            "Смещение не может быть отрицательным"
        }
        require(limit > 0) {
            "Количество команд должно быть больше нуля"
        }

        return repository.getTeams(
            name = name.trim().takeIf { it.isNotEmpty() },
            offset = offset,
            limit = limit
        )
    }
}