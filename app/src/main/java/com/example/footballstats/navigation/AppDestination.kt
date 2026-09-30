package com.example.footballstats.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    data object Teams : AppDestination(
        route = "teams",
        title = "Команды",
        icon = Icons.Default.SportsSoccer
    )

    data object Matches : AppDestination(
        route = "matches",
        title = "Матчи",
        icon = Icons.Default.Stadium
    )

    data object Competitions : AppDestination(
        route = "competitions",
        title = "Турниры",
        icon = Icons.Default.EmojiEvents
    )

    data object Favorites : AppDestination(
        route = "favorites",
        title = "Избранное",
        icon = Icons.Default.Favorite
    )

    data object TeamDetails {
        const val route = "team_details/{teamId}"

        fun createRoute(teamId: Int): String {
            return "team_details/$teamId"
        }
    }
}

val bottomNavigationItems = listOf(
    AppDestination.Teams,
    AppDestination.Matches,
    AppDestination.Competitions,
    AppDestination.Favorites
)