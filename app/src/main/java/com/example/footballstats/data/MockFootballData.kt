package com.example.footballstats.data

import com.example.footballstats.R
import com.example.footballstats.model.FootballTeam

object MockFootballData {

    val teams = listOf(
        FootballTeam(
            id = 1,
            name = "Arsenal",
            country = "England",
            stadium = "Emirates Stadium",
            coach = "Mikel Arteta",
            foundedYear = 1886,
            description = "Футбольный клуб из Лондона, выступающий в английской Премьер-лиге.",
            logoRes = R.drawable.arsenal
        ),
        FootballTeam(
            id = 2,
            name = "Barcelona",
            country = "Spain",
            stadium = "Camp Nou",
            coach = "Hansi Flick",
            foundedYear = 1899,
            description = "Один из самых известных футбольных клубов Испании.",
            logoRes = R.drawable.barcelona
        ),
        FootballTeam(
            id = 3,
            name = "Bayern Munich",
            country = "Germany",
            stadium = "Allianz Arena",
            coach = "Vincent Kompany",
            foundedYear = 1900,
            description = "Один из ведущих футбольных клубов Германии.",
            logoRes = R.drawable.bayern_munich
        ),
        FootballTeam(
            id = 4,
            name = "Inter Milan",
            country = "Italy",
            stadium = "San Siro",
            coach = "Cristian Chivu",
            foundedYear = 1908,
            description = "Итальянский футбольный клуб из Милана.",
            logoRes = R.drawable.inter_milan
        ),
        FootballTeam(
            id = 5,
            name = "Paris Saint-Germain",
            country = "France",
            stadium = "Parc des Princes",
            coach = "Luis Enrique",
            foundedYear = 1970,
            description = "Французский футбольный клуб из Парижа.",
            logoRes = R.drawable.psg
        ),
        FootballTeam(
            id = 6,
            name = "Liverpool",
            country = "England",
            stadium = "Anfield",
            coach = "Arne Slot",
            foundedYear = 1892,
            description = "Английский футбольный клуб из города Ливерпуль.",
            logoRes = R.drawable.liverpool
        ),
        FootballTeam(
            id = 7,
            name = "Real Madrid",
            country = "Spain",
            stadium = "Santiago Bernabeu",
            coach = "Xabi Alonso",
            foundedYear = 1902,
            description = "Один из наиболее титулованных футбольных клубов Европы.",
            logoRes = R.drawable.real_madrid
        ),
        FootballTeam(
            id = 8,
            name = "Borussia Dortmund",
            country = "Germany",
            stadium = "Signal Iduna Park",
            coach = "Niko Kovac",
            foundedYear = 1909,
            description = "Немецкий футбольный клуб из Дортмунда.",
            logoRes = R.drawable.borussia_dortmund
        ),
        FootballTeam(
            id = 9,
            name = "Juventus",
            country = "Italy",
            stadium = "Allianz Stadium",
            coach = "Igor Tudor",
            foundedYear = 1897,
            description = "Один из самых известных футбольных клубов Италии.",
            logoRes = R.drawable.juventus
        ),
        FootballTeam(
            id = 10,
            name = "Manchester City",
            country = "England",
            stadium = "Etihad Stadium",
            coach = "Pep Guardiola",
            foundedYear = 1880,
            description = "Английский футбольный клуб из Манчестера.",
            logoRes = R.drawable.manchester_city
        )
    )

    fun getTeamById(id: Int): FootballTeam? {
        return teams.find { it.id == id }
    }
}