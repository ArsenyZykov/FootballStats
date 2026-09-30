package com.example.footballstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.footballstats.navigation.FootballNavigation
import com.example.footballstats.ui.theme.FootballStatsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FootballStatsTheme {
                FootballNavigation()
            }
        }
    }
}