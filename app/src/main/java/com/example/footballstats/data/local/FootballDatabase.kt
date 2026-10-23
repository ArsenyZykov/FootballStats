package com.example.footballstats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteTeamEntity::class,
        TeamDetailsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class FootballDatabase : RoomDatabase() {

    abstract fun favoriteTeamsDao(): FavoriteTeamsDao

    abstract fun teamDetailsDao(): TeamDetailsDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `team_details_cache` (
                        `teamId` INTEGER NOT NULL,
                        `payload` TEXT NOT NULL,
                        `savedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`teamId`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}