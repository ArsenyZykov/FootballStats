package com.example.footballstats.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.footballstats.domain.model.TeamFilters
import com.example.footballstats.domain.model.TeamSortOrder
import com.example.footballstats.domain.repository.TeamFiltersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.teamFiltersDataStore by preferencesDataStore(
    name = "team_filters"
)

class TeamFiltersDataStore(
    context: Context
) : TeamFiltersRepository {

    private val dataStore = context.applicationContext.teamFiltersDataStore

    override val filters: Flow<TeamFilters> = dataStore.data
        .map { preferences ->
            val savedSortOrder = preferences[SORT_ORDER]

            TeamFilters(
                name = preferences[TEAM_NAME].orEmpty(),
                country = preferences[COUNTRY].orEmpty(),
                sortOrder = TeamSortOrder.entries.firstOrNull {
                    it.name == savedSortOrder
                } ?: TeamSortOrder.DEFAULT
            )
        }
        .distinctUntilChanged()

    override suspend fun saveFilters(filters: TeamFilters) {
        dataStore.edit { preferences ->
            val name = filters.name.trim()
            val country = filters.country.trim()

            if (name.isEmpty()) {
                preferences.remove(TEAM_NAME)
            } else {
                preferences[TEAM_NAME] = name
            }

            if (country.isEmpty()) {
                preferences.remove(COUNTRY)
            } else {
                preferences[COUNTRY] = country
            }

            if (filters.sortOrder == TeamSortOrder.DEFAULT) {
                preferences.remove(SORT_ORDER)
            } else {
                preferences[SORT_ORDER] = filters.sortOrder.name
            }
        }
    }

    private companion object {
        val TEAM_NAME = stringPreferencesKey("team_name")
        val COUNTRY = stringPreferencesKey("country")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }
}