package com.example.footballstats.domain.model

data class Team(
    val id: Int,
    val name: String,
    val country: String,
    val logoUrl: String?
)