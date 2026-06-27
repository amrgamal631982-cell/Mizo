package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY timestamp DESC")
    fun getAllScripts(): Flow<List<SavedScript>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScript): Long

    @Delete
    suspend fun deleteScript(script: SavedScript)

    @Query("DELETE FROM saved_scripts")
    suspend fun deleteAllScripts()
}
