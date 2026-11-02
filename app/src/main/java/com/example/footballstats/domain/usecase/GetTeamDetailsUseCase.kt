package com.example.footballstats.domain.usecase

import com.example.footballstats.domain.repository.TeamDetailsRepository
import com.example.footballstats.domain.repository.TeamDetailsResult

class GetTeamDetailsUseCase(
    private val repository: TeamDetailsRepository
) {

    suspend operator fun invoke(
        teamId: Int,
        refresh: Boolean = false
    ): TeamDetailsResult {
        require(teamId > 0) {
            "ID команды должен быть больше нуля"
        }

        return repository.getDetails(
            teamId = teamId,
            refresh = refresh
        )
    }
}