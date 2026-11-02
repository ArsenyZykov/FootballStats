package com.example.footballstats.domain.model

data class TeamFilters(
    val name: String = "",
    val country: String = "",
    val sortOrder: TeamSortOrder = TeamSortOrder.DEFAULT
) {
    val isActive: Boolean
        get() = name.isNotBlank() ||
                country.isNotBlank() ||
                sortOrder != TeamSortOrder.DEFAULT
}

enum class TeamSortOrder {
    DEFAULT,
    NAME_ASC,
    NAME_DESC
}