package com.example.footballstats.model

import androidx.annotation.DrawableRes

data class FootballTeam(
    val id: Int,
    val name: String,
    val country: String,
    val stadium: String,
    val coach: String,
    val foundedYear: Int,
    val description: String,
    @DrawableRes val logoRes: Int
)