package com.example.footballstats.domain.usecase

import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.model.TeamSortOrder
import com.example.footballstats.domain.repository.TeamsRepository
import java.text.Collator
import java.util.Locale

class GetTeamsUseCase(
    private val repository: TeamsRepository
) {

    suspend operator fun invoke(
        name: String = "",
        offset: Int = 0,
        limit: Int = 20,
        country: String = "",
        sortOrder: TeamSortOrder = TeamSortOrder.DEFAULT
    ): List<Team> {
        require(offset >= 0) {
            "Смещение не может быть отрицательным"
        }

        require(limit > 0) {
            "Количество команд должно быть больше нуля"
        }

        val teams = repository.getTeams(
            name = name.trim().takeIf { it.isNotEmpty() },
            offset = offset,
            limit = limit,
            country = country.trim().takeIf { it.isNotEmpty() }
        )

        if (sortOrder == TeamSortOrder.DEFAULT) {
            return teams
        }

        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }

        val comparator = Comparator<Team> { first, second ->
            val comparison = collator.compare(first.name, second.name)

            if (comparison != 0) {
                comparison
            } else {
                first.id.compareTo(second.id)
            }
        }

        return when (sortOrder) {
            TeamSortOrder.DEFAULT -> teams
            TeamSortOrder.NAME_ASC -> teams.sortedWith(comparator)
            TeamSortOrder.NAME_DESC -> teams.sortedWith(
                comparator.reversed()
            )
        }
    }
}