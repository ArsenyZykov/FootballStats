package com.example.footballstats.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.footballstats.ui.competitions.CompetitionsScreen
import com.example.footballstats.ui.favorites.FavoritesRoute
import com.example.footballstats.ui.filters.TeamFiltersRoute
import com.example.footballstats.ui.matches.MatchesRoute
import com.example.footballstats.ui.profile.EditProfileRoute
import com.example.footballstats.ui.profile.ProfileRoute
import com.example.footballstats.ui.teams.TeamDetailsRoute
import com.example.footballstats.ui.teams.TeamsRoute

private const val TEAM_FILTERS_ROUTE = "team_filters"
private const val EDIT_PROFILE_ROUTE = "edit_profile"

@Composable
fun FootballNavigation() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val showBottomBar = currentRoute != AppDestination.TeamDetails.route &&
        currentRoute != TEAM_FILTERS_ROUTE && currentRoute != EDIT_PROFILE_ROUTE

    Scaffold(bottomBar = {
        if (showBottomBar) FootballBottomBar(navController)
    }) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Teams.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppDestination.Teams.route) {
                TeamsRoute(
                    onOpenFilters = { navController.navigate(TEAM_FILTERS_ROUTE) { launchSingleTop = true } },
                    onTeamClick = { navController.navigate(AppDestination.TeamDetails.createRoute(it)) }
                )
            }
            composable(TEAM_FILTERS_ROUTE) {
                TeamFiltersRoute(
                    onBackClick = { navController.popBackStack() },
                    onFiltersSaved = { navController.popBackStack() }
                )
            }
            composable(AppDestination.Matches.route) { MatchesRoute() }
            composable(AppDestination.Competitions.route) { CompetitionsScreen() }
            composable(AppDestination.Profile.route) {
                ProfileRoute(onEdit = { navController.navigate(EDIT_PROFILE_ROUTE) { launchSingleTop = true } })
            }
            composable(EDIT_PROFILE_ROUTE) {
                EditProfileRoute(onClose = { navController.popBackStack() })
            }
            composable(AppDestination.Favorites.route) {
                FavoritesRoute(onTeamClick = {
                    navController.navigate(AppDestination.TeamDetails.createRoute(it))
                })
            }
            composable(
                route = AppDestination.TeamDetails.route,
                arguments = listOf(navArgument("teamId") { type = NavType.IntType })
            ) {
                TeamDetailsRoute(onBackClick = { navController.popBackStack() })
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
