package com.example.footballstats.di

import com.example.footballstats.data.remote.FootballApiClient
import com.example.footballstats.data.repository.MatchesRepositoryImpl
import com.example.footballstats.data.repository.TeamsRepositoryImpl
import com.example.footballstats.domain.usecase.GetMatchesUseCase
import com.example.footballstats.domain.usecase.GetTeamsUseCase

object FootballDependencies {

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
}