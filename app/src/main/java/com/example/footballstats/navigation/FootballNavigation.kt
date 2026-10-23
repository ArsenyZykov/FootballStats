package com.example.footballstats.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.footballstats.ui.matches.MatchesScreen
import com.example.footballstats.ui.teams.TeamDetailsScreen
import com.example.footballstats.ui.teams.TeamsScreen
import com.example.footballstats.ui.teams.TeamsViewModel

@Composable
fun FootballNavigation() {
    val navController = rememberNavController()
    val teamsViewModel: TeamsViewModel = viewModel()
    val uiState by teamsViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != AppDestination.TeamDetails.route

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
                    onQueryChange = teamsViewModel::onSearchQueryChange,
                    onSearch = teamsViewModel::searchTeams,
                    onClearSearch = teamsViewModel::clearSearch,
                    onRetry = teamsViewModel::retry,
                    onTeamClick = { teamId ->
                        navController.navigate(
                            AppDestination.TeamDetails.createRoute(teamId)
                        )
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
                FavoritesScreen()
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
                val team = uiState.teams.find { it.id == teamId }

                when {
                    team != null -> {
                        TeamDetailsScreen(
                            team = team,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    uiState.isLoading -> {
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
                                text = uiState.errorMessage
                                    ?: "Команда недоступна. Вернитесь к списку."
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