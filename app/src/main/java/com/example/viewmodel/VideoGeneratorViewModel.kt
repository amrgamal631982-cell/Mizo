package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.database.AppDatabase
import com.example.database.SavedScript
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val script: SavedScript) : UiState
    data class Error(val message: String) : UiState
}

// Data class to represent structured Scene inside the UI
data class SceneItem(
    val timeRange: String,
    val visualPrompt: String,
    val audioVO: String,
    val textOnScreen: String
)

class VideoGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.savedScriptDao()

    // Tab state
    val selectedTab = MutableStateFlow(0) // 0: Text-to-Video, 1: Image-to-Video, 2: History

    // Text to Video Input state
    val textConcept = MutableStateFlow("")
    val textStyle = MutableStateFlow("Cinematic 3D")
    val textDuration = MutableStateFlow("15 Seconds")
    val textPlatform = MutableStateFlow("TikTok / Reels (9:16)")
    val textLanguage = MutableStateFlow("English")

    // Image to Video Input state
    val imageDescription = MutableStateFlow("")
    val imagePhysics = MutableStateFlow("Water Ripples")
    val imageCamera = MutableStateFlow("Slow-motion Zoom")
    val selectedPresetIndex = MutableStateFlow(0) // 0: Cyberpunk, 1: Perfume, 2: Dragon, 3: Custom
    val imageLanguage = MutableStateFlow("English")

    // General UI State for current generation
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Saved History
    private val _history = MutableStateFlow<List<SavedScript>>(emptyList())
    val history: StateFlow<List<SavedScript>> = _history.asStateFlow()

    init {
        // Load history from database
        viewModelScope.launch {
            dao.getAllScripts()
                .catch { e ->
                    _uiState.value = UiState.Error("Failed to load history: ${e.localizedMessage}")
                }
                .collectLatest { list ->
                    _history.value = list
                }
        }
    }

    fun selectTab(index: Int) {
        selectedTab.value = index
        _uiState.value = UiState.Idle
    }

    fun generateTextToVideo() {
        if (textConcept.value.isBlank()) {
            _uiState.value = UiState.Error("Please enter your product concept or raw message!")
            return
        }

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val jsonResponse = GeminiClient.generateScriptAndPrompts(
                    concept = textConcept.value,
                    style = textStyle.value,
                    duration = textDuration.value,
                    platform = textPlatform.value,
                    language = textLanguage.value
                )

                val parsedScript = parseJsonToSavedScript(jsonResponse, textConcept.value, textDuration.value)
                
                // Save to Room Database automatically
                dao.insertScript(parsedScript)

                _uiState.value = UiState.Success(parsedScript)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Generation failed: ${e.localizedMessage}")
            }
        }
    }

    fun generateImageToVideo() {
        val desc = if (selectedPresetIndex.value == 3) {
            imageDescription.value
        } else {
            getPresetDescription(selectedPresetIndex.value)
        }

        if (desc.isBlank()) {
            _uiState.value = UiState.Error("Please enter a custom image description!")
            return
        }

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val jsonResponse = GeminiClient.generateImageAnimationPrompt(
                    imageDescription = desc,
                    physicsStyle = imagePhysics.value,
                    cameraMotion = imageCamera.value,
                    language = imageLanguage.value
                )

                val parsedScript = parseJsonToSavedScript(jsonResponse, desc, "5 Seconds")

                // Save to Room Database automatically
                dao.insertScript(parsedScript)

                _uiState.value = UiState.Success(parsedScript)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Generation failed: ${e.localizedMessage}")
            }
        }
    }

    fun deleteScript(script: SavedScript) {
        viewModelScope.launch {
            dao.deleteScript(script)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.deleteAllScripts()
        }
    }

    private fun parseJsonToSavedScript(jsonString: String, originalConcept: String, fallbackDuration: String): SavedScript {
        return try {
            // Trim markdown backticks if any
            var cleanedJson = jsonString.trim()
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7)
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3)
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length - 3)
            }
            cleanedJson = cleanedJson.trim()

            val json = JSONObject(cleanedJson)
            val title = json.optString("title", "AI Generation")
            val duration = json.optString("estimatedDuration", fallbackDuration)
            val masterPrompt = json.optString("masterPrompt", "")
            val scenesArray = json.optJSONArray("scenes") ?: JSONArray()
            val callToAction = json.optString("callToAction", "")

            SavedScript(
                title = title,
                concept = originalConcept,
                duration = duration,
                masterPrompt = masterPrompt,
                sceneBreakdownJson = scenesArray.toString(),
                callToAction = callToAction
            )
        } catch (e: Exception) {
            // High durability fallback if JSON parsing fails completely
            SavedScript(
                title = "AI Gen Campaign",
                concept = originalConcept,
                duration = fallbackDuration,
                masterPrompt = jsonString,
                sceneBreakdownJson = "[]",
                callToAction = "Create your compelling CTA now!"
            )
        }
    }

    fun parseScenes(jsonString: String): List<SceneItem> {
        val list = mutableListOf<SceneItem>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SceneItem(
                        timeRange = obj.optString("timeRange", "0:00 - 0:05"),
                        visualPrompt = obj.optString("visualPrompt", ""),
                        audioVO = obj.optString("audioVO", ""),
                        textOnScreen = obj.optString("textOnScreen", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback
        }
        return list
    }

    private fun getPresetDescription(index: Int): String {
        return when (index) {
            0 -> "Vibrant neon cyberpunk street cityscape with glowing signs and rain reflections"
            1 -> "Sleek luxury perfume bottle sitting on crystal clear water with delicate ripples"
            2 -> "Cute 3D Pixar baby fantasy dragon with large expressive eyes sitting on treasure chest"
            else -> ""
        }
    }
}
