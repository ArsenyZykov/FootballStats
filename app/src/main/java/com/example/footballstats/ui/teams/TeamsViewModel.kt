package com.example.footballstats.ui.teams

import androidx.lifecycle.ViewModel
import com.example.footballstats.data.MockFootballData
import com.example.footballstats.model.FootballTeam

class TeamsViewModel : ViewModel() {

    val teams: List<FootballTeam> = MockFootballData.teams

    fun getTeamById(id: Int): FootballTeam? {
        return MockFootballData.getTeamById(id)
    }
}