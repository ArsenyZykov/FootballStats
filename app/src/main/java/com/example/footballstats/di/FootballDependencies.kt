package com.example.footballstats.di

import android.content.Context
import androidx.room.Room
import com.example.footballstats.data.local.FootballDatabase
import com.example.footballstats.data.local.TeamFiltersDataStore
import com.example.footballstats.data.remote.FootballApiClient
import com.example.footballstats.data.repository.FavoritesRepositoryImpl
import com.example.footballstats.data.repository.MatchesRepositoryImpl
import com.example.footballstats.data.repository.TeamDetailsRepositoryImpl
import com.example.footballstats.data.repository.TeamsRepositoryImpl
import com.example.footballstats.domain.repository.TeamFiltersRepository
import com.example.footballstats.domain.usecase.GetFavoriteTeamsUseCase
import com.example.footballstats.domain.usecase.GetMatchesUseCase
import com.example.footballstats.domain.usecase.GetTeamDetailsUseCase
import com.example.footballstats.domain.usecase.GetTeamsUseCase
import com.example.footballstats.domain.usecase.SetFavoriteTeamUseCase
import com.example.footballstats.ui.common.FiltersBadgeCache

object FootballDependencies {

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireAppContext(): Context {
        check(::appContext.isInitialized) {
            "Вызовите FootballDependencies.initialize(context)"
        }

        return appContext
    }

    private val teamsRepository = TeamsRepositoryImpl(
        api = FootballApiClient.api
    )

    private val matchesRepository = MatchesRepositoryImpl(
        api = FootballApiClient.api
    )

    val getTeamsUseCase = GetTeamsUseCase(
        repository = teamsRepository
    )

    val getMatchesUseCase = GetMatchesUseCase(
        repository = matchesRepository
    )

    private val database: FootballDatabase by lazy {
        Room.databaseBuilder(
            requireAppContext(),
            FootballDatabase::class.java,
            "football_stats.db"
        )
            .addMigrations(FootballDatabase.MIGRATION_1_2)
            .build()
    }

    private val favoritesRepository by lazy {
        FavoritesRepositoryImpl(
            dao = database.favoriteTeamsDao()
        )
    }

    val getFavoriteTeamsUseCase by lazy {
        GetFavoriteTeamsUseCase(
            repository = favoritesRepository
        )
    }

    val setFavoriteTeamUseCase by lazy {
        SetFavoriteTeamUseCase(
            repository = favoritesRepository
        )
    }

    private val teamDetailsRepository by lazy {
        TeamDetailsRepositoryImpl(
            api = FootballApiClient.api,
            dao = database.teamDetailsDao()
        )
    }

    val getTeamDetailsUseCase by lazy {
        GetTeamDetailsUseCase(
            repository = teamDetailsRepository
        )
    }

    val teamFiltersRepository: TeamFiltersRepository by lazy {
        TeamFiltersDataStore(
            context = requireAppContext()
        )
    }

    val filtersBadgeCache: FiltersBadgeCache by lazy {
        FiltersBadgeCache()
    }
}