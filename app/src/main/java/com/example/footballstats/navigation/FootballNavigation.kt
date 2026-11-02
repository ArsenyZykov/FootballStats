package com.example.footballstats.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.footballstats.ui.competitions.CompetitionsScreen
import com.example.footballstats.ui.favorites.FavoritesScreen
import com.example.footballstats.ui.favorites.FavoritesViewModel
import com.example.footballstats.ui.filters.TeamFiltersScreen
import com.example.footballstats.ui.matches.MatchesScreen
import com.example.footballstats.ui.teams.TeamDetailsScreen
import com.example.footballstats.ui.teams.TeamsScreen
import com.example.footballstats.ui.teams.TeamsViewModel

private const val TEAM_FILTERS_ROUTE = "team_filters"

@Composable
fun FootballNavigation() {
    val navController = rememberNavController()

    val teamsViewModel: TeamsViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()

    val uiState by teamsViewModel.uiState.collectAsState()
    val favoritesState by favoritesViewModel.uiState.collectAsState()
    val showFilterBadge by teamsViewModel.showFilterBadge.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar =
        currentRoute != AppDestination.TeamDetails.route &&
                currentRoute != TEAM_FILTERS_ROUTE

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FootballBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Teams.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Teams.route) {
                TeamsScreen(
                    uiState = uiState,
                    showFilterBadge = showFilterBadge,
                    onOpenFilters = {
                        navController.navigate(TEAM_FILTERS_ROUTE) {
                            launchSingleTop = true
                        }
                    },
                    onRetry = teamsViewModel::retry,
                    onTeamClick = { teamId ->
                        navController.navigate(
                            AppDestination.TeamDetails.createRoute(teamId)
                        )
                    }
                )
            }

            composable(TEAM_FILTERS_ROUTE) {
                TeamFiltersScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onFiltersSaved = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestination.Matches.route) {
                MatchesScreen()
            }

            composable(AppDestination.Competitions.route) {
                CompetitionsScreen()
            }

            composable(AppDestination.Favorites.route) {
                FavoritesScreen(
                    uiState = favoritesState,
                    onRetry = favoritesViewModel::retry,
                    onRemoveFavorite = { team ->
                        favoritesViewModel.setFavorite(
                            team = team,
                            isFavorite = false
                        )
                    },
                    onTeamClick = { teamId ->
                        navController.navigate(
                            AppDestination.TeamDetails.createRoute(teamId)
                        )
                    }
                )
            }

            composable(
                route = AppDestination.TeamDetails.route,
                arguments = listOf(
                    navArgument("teamId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getInt("teamId")

                val availableTeam =
                    uiState.teams.find { it.id == teamId }
                        ?: favoritesState.teams.find { it.id == teamId }

                // Сохраняем открытую карточку в памяти, чтобы удаление
                // из избранного не закрыло её во время просмотра.
                val retainedTeam = remember(backStackEntry) {
                    androidx.compose.runtime.mutableStateOf<
                            com.example.footballstats.domain.model.Team?
                            >(null)
                }

                androidx.compose.runtime.LaunchedEffect(availableTeam) {
                    if (availableTeam != null) {
                        retainedTeam.value = availableTeam
                    }
                }

                val team = availableTeam ?: retainedTeam.value

                when {
                    team != null -> {
                        val isFavorite = favoritesState.teams.any {
                            it.id == team.id
                        }

                        TeamDetailsScreen(
                            team = team,
                            isFavorite = isFavorite,
                            canChangeFavorite =
                                favoritesState.hasLoaded &&
                                        team.id !in favoritesState.pendingTeamIds,
                            favoriteError = favoritesState.errorMessage,
                            onToggleFavorite = {
                                favoritesViewModel.setFavorite(
                                    team = team,
                                    isFavorite = !isFavorite
                                )
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    uiState.isLoading || favoritesState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Команда недоступна. " +
                                        "Вернитесь к списку."
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Text("К списку")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FootballBottomBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavigationItems.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(destination.title)
                }
            )
        }
    }
}