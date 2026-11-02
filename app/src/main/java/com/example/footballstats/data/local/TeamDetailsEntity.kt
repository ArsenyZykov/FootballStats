package com.example.footballstats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_details_cache")
data class TeamDetailsEntity(
    @PrimaryKey
    val teamId: Int,
    val payload: String,
    val savedAt: Long
)