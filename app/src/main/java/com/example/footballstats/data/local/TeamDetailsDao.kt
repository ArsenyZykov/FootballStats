package com.example.footballstats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamDetailsDao {

    @Query(
        """
        SELECT * FROM team_details_cache
        WHERE teamId = :teamId
        """
    )
    suspend fun getById(teamId: Int): TeamDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: TeamDetailsEntity)
}