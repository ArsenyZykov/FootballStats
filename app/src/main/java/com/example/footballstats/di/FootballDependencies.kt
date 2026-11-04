package com.example.footballstats.di

import android.content.Context
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.footballstats.data.local.FootballDatabase
import com.example.footballstats.data.local.TeamFiltersDataStore
import com.example.footballstats.data.reminder.AndroidLessonReminderScheduler
import com.example.footballstats.data.remote.FootballApiClient
import com.example.footballstats.data.repository.FavoritesRepositoryImpl
import com.example.footballstats.data.repository.MatchesRepositoryImpl
import com.example.footballstats.data.repository.ProfileRepositoryImpl
import com.example.footballstats.data.repository.TeamDetailsRepositoryImpl
import com.example.footballstats.data.repository.TeamsRepositoryImpl
import com.example.footballstats.domain.reminder.LessonReminderScheduler
import com.example.footballstats.domain.repository.TeamFiltersRepository
import com.example.footballstats.domain.usecase.GetFavoriteTeamsUseCase
import com.example.footballstats.domain.usecase.GetMatchesUseCase
import com.example.footballstats.domain.usecase.GetTeamDetailsUseCase
import com.example.footballstats.domain.usecase.GetTeamsUseCase
import com.example.footballstats.domain.usecase.SetFavoriteTeamUseCase
import com.example.footballstats.ui.common.FiltersBadgeCache
import com.example.footballstats.ui.common.TeamSummaryCache
import com.example.footballstats.ui.favorites.FavoritesViewModel
import com.example.footballstats.ui.filters.TeamFiltersViewModel
import com.example.footballstats.ui.matches.MatchesViewModel
import com.example.footballstats.ui.profile.EditProfileViewModel
import com.example.footballstats.ui.profile.ProfileViewModel
import com.example.footballstats.ui.teams.TeamDetailsViewModel
import com.example.footballstats.ui.teams.TeamsViewModel

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

    private val teamSummaryCache = TeamSummaryCache()

    private val profileRepository by lazy {
        ProfileRepositoryImpl(requireAppContext())
    }

    val lessonReminderScheduler: LessonReminderScheduler by lazy {
        AndroidLessonReminderScheduler(requireAppContext())
    }

    val viewModelFactory = viewModelFactory {
        initializer {
            ProfileViewModel(profileRepository)
        }

        initializer {
            EditProfileViewModel(
                repository = profileRepository,
                savedState = createSavedStateHandle(),
                reminderScheduler = lessonReminderScheduler
            )
        }

        initializer {
            TeamsViewModel(
                getTeamsUseCase = getTeamsUseCase,
                filtersRepository = teamFiltersRepository,
                badgeCache = filtersBadgeCache,
                summaryCache = teamSummaryCache
            )
        }

        initializer {
            MatchesViewModel(getMatchesUseCase)
        }

        initializer {
            FavoritesViewModel(
                getFavoriteTeamsUseCase,
                setFavoriteTeamUseCase
            )
        }

        initializer {
            TeamFiltersViewModel(
                teamFiltersRepository,
                filtersBadgeCache
            )
        }

        initializer {
            TeamDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                getTeamDetails = getTeamDetailsUseCase,
                getFavoriteTeams = getFavoriteTeamsUseCase,
                setFavoriteTeam = setFavoriteTeamUseCase,
                summaryCache = teamSummaryCache
            )
        }
    }
}