package com.example.footballstats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val country: String,
    val logoUrl: String?
)