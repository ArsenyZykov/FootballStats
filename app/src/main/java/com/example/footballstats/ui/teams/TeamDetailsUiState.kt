package com.example.footballstats.ui.teams

import com.example.footballstats.domain.model.Team
import com.example.footballstats.domain.repository.TeamDetailsIssue
import com.example.footballstats.domain.repository.TeamDetailsResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TeamDetailsUiState(
    val content: TeamDetailsUiModel? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val favoritesLoaded: Boolean = false,
    val isSavingFavorite: Boolean = false,
    val favoriteError: String? = null
) {
    val canChangeFavorite: Boolean
        get() = content != null && favoritesLoaded && !isSavingFavorite
}

data class DetailsFieldUiModel(val label: String, val value: String)
data class DetailsSectionUiModel(val title: String, val fields: List<DetailsFieldUiModel>)
data class TeamDetailsUiModel(
    val team: Team,
    val shareText: String,
    val sections: List<DetailsSectionUiModel> = emptyList(),
    val players: List<String> = emptyList(),
    val showAllPlayersLabel: String = "",
    val tournaments: List<DetailsFieldUiModel> = emptyList(),
    val sourceLabel: String? = null,
    val notice: String? = null
)

internal fun teamDetailsUiModel(team: Team, result: TeamDetailsResult?): TeamDetailsUiModel {
    val details = result?.details
    val displayedTeam = team.copy(
        name = details?.name ?: team.name,
        country = details?.country ?: team.country
    )
    val shareText = buildString {
        appendLine(displayedTeam.name)
        appendLine("Страна: ${displayedTeam.country}")
        details?.code?.let { appendLine("Код: $it") }
        details?.foundedYear?.let { appendLine("Год основания: $it") }
        details?.stadiumName?.let { appendLine("Стадион: $it") }
        details?.stadiumCity?.let { appendLine("Город: $it") }
        appendLine("Источник: SStats.net")
    }.trim()
    if (details == null) return TeamDetailsUiModel(displayedTeam, shareText)

    fun field(label: String, value: String?) = DetailsFieldUiModel(label, value ?: "Нет данных")
    return TeamDetailsUiModel(
        team = displayedTeam,
        shareText = shareText,
        sections = listOf(
            DetailsSectionUiModel("О команде", listOf(
                field("Код команды", details.code),
                field("Год основания", details.foundedYear?.toString())
            )),
            DetailsSectionUiModel("Стадион", listOf(
                field("Название", details.stadiumName),
                field("Город", details.stadiumCity),
                field("Адрес", details.stadiumAddress),
                field("Вместимость", details.stadiumCapacity?.let {
                    NumberFormat.getIntegerInstance(Locale.getDefault()).format(it)
                })
            )),
            DetailsSectionUiModel("Тренер по данным API", listOf(field("Тренер", details.coachName)))
        ),
        players = details.players.map { "• ${it.name}" },
        showAllPlayersLabel = "Показать всех (${details.players.size})",
        tournaments = details.seasons.groupBy { it.leagueId }.values
            .sortedBy { it.first().leagueName }.map { seasons ->
                DetailsFieldUiModel(
                    seasons.first().leagueName,
                    "Годы сезонов: " + seasons.map { it.year }.distinct().sortedDescending().joinToString()
                )
            },
        sourceLabel = (if (result.isFromCache) "Сохранённая копия" else "Загружено из SStats.net") +
            " · " + SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(result.updatedAt)),
        notice = when (result.issue) {
            TeamDetailsIssue.REFRESH_FAILED -> "Не удалось обновить данные. Показана сохранённая копия."
            TeamDetailsIssue.CACHE_WRITE_FAILED -> "Данные загружены, но не удалось сохранить их для просмотра без интернета."
            null -> null
        }
    )
}
