package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val concept: String,
    val duration: String,
    val masterPrompt: String,
    val sceneBreakdownJson: String, // Store JSON string of scenes
    val callToAction: String,
    val timestamp: Long = System.currentTimeMillis()
)
