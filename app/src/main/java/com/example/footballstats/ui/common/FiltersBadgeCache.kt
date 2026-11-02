package com.example.footballstats.ui.common

import com.example.footballstats.domain.model.TeamFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltersBadgeCache {

    private val _showBadge = MutableStateFlow(false)

    val showBadge: StateFlow<Boolean> = _showBadge.asStateFlow()

    fun update(filters: TeamFilters) {
        _showBadge.value = filters.isActive
    }
}