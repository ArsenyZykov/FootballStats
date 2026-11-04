package com.example.footballstats.ui.common

import com.example.footballstats.domain.model.Team

class TeamSummaryCache {
    private val teams = mutableMapOf<Int, Team>()

    fun putAll(items: List<Team>) {
        items.forEach { teams[it.id] = it }
    }

    fun get(id: Int): Team? = teams[id]
}
