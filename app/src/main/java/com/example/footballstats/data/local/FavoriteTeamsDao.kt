package com.example.footballstats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTeamsDao {

    @Query(
        """
        SELECT * FROM favorite_teams
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeTeams(): Flow<List<FavoriteTeamEntity>>

    @Query("SELECT * FROM favorite_teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Int): FavoriteTeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: FavoriteTeamEntity)

    @Query("DELETE FROM favorite_teams WHERE id = :teamId")
    suspend fun deleteById(teamId: Int)
}