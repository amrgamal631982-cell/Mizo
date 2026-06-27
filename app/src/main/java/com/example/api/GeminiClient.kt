package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateScriptAndPrompts(
        concept: String,
        style: String,
        duration: String,
        platform: String,
        language: String // "English" or "Arabic"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder!")
            return@withContext createMockResponse(concept, style, duration, platform, language, "Please enter your real Gemini API Key in the AI Studio Secrets panel.")
        }

        val prompt = buildPromptForScript(concept, style, duration, platform, language)

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful) {
                    val errMsg = "HTTP ${response.code}: ${response.message}\n$bodyString"
                    Log.e(TAG, "API Error: $errMsg")
                    return@withContext createMockResponse(concept, style, duration, platform, language, "API Error: HTTP ${response.code}")
                }

                if (bodyString.isNullOrEmpty()) {
                    return@withContext createMockResponse(concept, style, duration, platform, language, "Empty response from server")
                }

                // Parse the Gemini REST API format: candidates[0].content.parts[0].text
                val root = JSONObject(bodyString)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotEmpty()) {
                            return@withContext text
                        }
                    }
                }
                return@withContext createMockResponse(concept, style, duration, platform, language, "Could not extract text from response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception", e)
            return@withContext createMockResponse(concept, style, duration, platform, language, "Error: ${e.localizedMessage}")
        }
    }

    suspend fun generateImageAnimationPrompt(
        imageDescription: String,
        physicsStyle: String,
        cameraMotion: String,
        language: String // "English" or "Arabic"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder!")
            return@withContext createMockImageAnimationResponse(imageDescription, physicsStyle, cameraMotion, language, "Please enter your real Gemini API Key in the AI Studio Secrets panel.")
        }

        val prompt = buildPromptForImageToVideo(imageDescription, physicsStyle, cameraMotion, language)

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Error: HTTP ${response.code}")
                    return@withContext createMockImageAnimationResponse(imageDescription, physicsStyle, cameraMotion, language, "API Error: HTTP ${response.code}")
                }

                if (bodyString.isNullOrEmpty()) {
                    return@withContext createMockImageAnimationResponse(imageDescription, physicsStyle, cameraMotion, language, "Empty response")
                }

                val root = JSONObject(bodyString)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotEmpty()) {
                            return@withContext text
                        }
                    }
                }
                return@withContext createMockImageAnimationResponse(imageDescription, physicsStyle, cameraMotion, language, "Could not extract text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            return@withContext createMockImageAnimationResponse(imageDescription, physicsStyle, cameraMotion, language, "Error: ${e.localizedMessage}")
        }
    }

    private fun buildPromptForScript(
        concept: String,
        style: String,
        duration: String,
        platform: String,
        language: String
    ): String {
        val langInstruction = if (language == "Arabic") {
            "IMPORTANT: Output everything except the JSON keys in Arabic. The values should be in Arabic, maintaining a highly creative, marketing-savvy, and encouraging tone suitable for a premium ad campaign."
        } else {
            "IMPORTANT: Output in English, maintaining a highly creative, marketing-savvy, and encouraging tone."
        }

        return """
            You are the core AI engine for a cutting-edge 100% free AI Video & Advertisement Generator application.
            Your mission is to transform the user's concept into a highly engaging, creative, and production-ready video script and prompt, optimized for the world's most advanced video models (like Sora, Runway Gen-3, Luma, and Kling).
            
            User Input details:
            - Concept/Product: "$concept"
            - Style/Vibe: "$style"
            - Target Duration: "$duration"
            - Platform Format: "$platform"
            
            $langInstruction
            
            You MUST return a valid JSON object matching the following structure exactly (no extra text outside the JSON, no markdown outside the JSON block, just the raw JSON object or JSON-wrapped string):
            {
              "title": "A short, catchy, professional title for the video/ad campaign",
              "estimatedDuration": "$duration",
              "masterPrompt": "A single hyper-detailed, cinematic master prompt combining style, lighting, camera angles, and atmosphere, which the user can copy and paste directly into Sora/Runway/Kling to generate the complete video",
              "scenes": [
                {
                  "timeRange": "0:00 - 0:05",
                  "visualPrompt": "Cinematic scene prompt: precise camera angles (e.g. tracking shot, drone shot), specific lighting (e.g. cinematic, volumetric, neon, golden hour), and precise motion/subject details",
                  "audioVO": "Recommended Voiceover (VO) text or audio sound effects",
                  "textOnScreen": "Recommended text or subtitles to overlay on this scene"
                }
              ],
              "callToAction": "A powerful, marketing-savvy Call to Action (CTA) recommendation to end the video"
            }
            
            Make sure to generate 3 to 5 scenes depending on the duration. Be extremely detailed, creative, and professional. Use high-end cinematic terms (e.g., volumetric lighting, ray tracing, 8k, bokeh, tracking shot, cinematic drone sweep, ultra-realistic).
        """.trimIndent()
    }

    private fun buildPromptForImageToVideo(
        imageDescription: String,
        physicsStyle: String,
        cameraMotion: String,
        language: String
    ): String {
        val langInstruction = if (language == "Arabic") {
            "IMPORTANT: Output everything except the JSON keys in Arabic. The values must be in Arabic, encouraging and professional."
        } else {
            "IMPORTANT: Output in English, high-end professional cinematic tone."
        }

        return """
            You are the core AI engine for a cutting-edge 100% free AI Video & Advertisement Generator application.
            Your mission is to generate a high-end motion animation prompt that instructs advanced AI video models (like Sora, Runway Gen-3, Luma, Kling) on how to animate an image flawlessly.
            
            Image/Subject description: "$imageDescription"
            Physics/Fluid dynamics style: "$physicsStyle"
            Camera Motion/Framing: "$cameraMotion"
            
            $langInstruction
            
            You MUST return a valid JSON object matching the following structure exactly (no extra text, just raw JSON):
            {
              "title": "A short descriptive name of the motion concept",
              "estimatedDuration": "5 Seconds",
              "masterPrompt": "A hyper-detailed, cinematic motion animation prompt for next-gen video models. For example: Animate the [subject] with realistic [physics], [camera motion], maintaining perfect facial/object consistency, 8k resolution, flawless photorealistic detail, slow-motion physics.",
              "scenes": [
                {
                  "timeRange": "0:00 - 0:05",
                  "visualPrompt": "Detailed step-by-step instructions of how the animation starts, flows, and peaks. Specify exact physics, speed of animation, lighting shift, and particle flow.",
                  "audioVO": "Recommended ambient sound effects or soundtrack mood",
                  "textOnScreen": "Recommended text or logo placement advice"
                }
              ],
              "callToAction": "Suggested CTA or branding overlay for this animation"
            }
        """.trimIndent()
    }

    private fun createMockResponse(
        concept: String,
        style: String,
        duration: String,
        platform: String,
        language: String,
        errorNote: String
    ): String {
        val isArabic = language == "Arabic"
        val json = JSONObject()
        if (isArabic) {
            json.put("title", "حملة إعلانية لـ $concept ($errorNote)")
            json.put("estimatedDuration", duration)
            json.put("masterPrompt", "لقطة سينمائية مذهلة لـ $concept بأسلوب $style، إضاءة سينمائية ثلاثية الأبعاد، دقة عالية 8k، حركة كاميرا سلسة تنساب حول المنتج.")
            
            val scenes = JSONArray().apply {
                put(JSONObject().apply {
                    put("timeRange", "0:00 - 0:05")
                    put("visualPrompt", "لقطة تقريبية تبدأ بالتركيز على $concept بأسلوب $style مع إضاءة خافتة وتأثيرات ضبابية ناعمة.")
                    put("audioVO", "مرحباً بكم في عالم الابتكار والتميز مع منتجنا الجديد!")
                    put("textOnScreen", "اكتشف القوة الحقيقية لـ $concept")
                })
                put(JSONObject().apply {
                    put("timeRange", "0:05 - 0:15")
                    put("visualPrompt", "حركة كاميرا بانورامية سريعة تكشف عن تفاصيل $concept مع إضاءة حيوية نيون خلفية.")
                    put("audioVO", "مصمم خصيصاً ليناسب تطلعاتك ويلبي احتياجاتك اليومية بكل سهولة.")
                    put("textOnScreen", "تصميم عصري وأداء لا مثيل له")
                })
            }
            json.put("scenes", scenes)
            json.put("callToAction", "احصل على منتجك الآن واستمتع بخصم خاص لفترة محدودة! اضغط على الرابط في الوصف.")
        } else {
            json.put("title", "Ad Campaign for $concept ($errorNote)")
            json.put("estimatedDuration", duration)
            json.put("masterPrompt", "A gorgeous cinematic 8k shot of $concept in $style style, with volumetric lighting, shallow depth of field, drone tracking shot capturing every fine detail, ultra-realistic textures.")
            
            val scenes = JSONArray().apply {
                put(JSONObject().apply {
                    put("timeRange", "0:00 - 0:05")
                    put("visualPrompt", "A slow-motion cinematic tracking shot of $concept with golden hour lighting, gentle bokeh in the background, showing the premium texture and design.")
                    put("audioVO", "Experience the pinnacle of innovation and premium quality with $concept.")
                    put("textOnScreen", "Introducing the All-New $concept")
                })
                put(JSONObject().apply {
                    put("timeRange", "0:05 - 0:15")
                    put("visualPrompt", "A dynamic orbital camera shot revealing $concept in action under cool studio volumetric lighting, high contrast.")
                    put("audioVO", "Engineered to transform your daily routine and keep you ahead of the game.")
                    put("textOnScreen", "Redefine Your Standards")
                })
            }
            json.put("scenes", scenes)
            json.put("callToAction", "Upgrade your life today. Click the link to shop now and get 20% off!")
        }
        return json.toString()
    }

    private fun createMockImageAnimationResponse(
        imageDescription: String,
        physicsStyle: String,
        cameraMotion: String,
        language: String,
        errorNote: String
    ): String {
        val isArabic = language == "Arabic"
        val json = JSONObject()
        if (isArabic) {
            json.put("title", "تحريك الصورة: $imageDescription ($errorNote)")
            json.put("estimatedDuration", "5 ثوانٍ")
            json.put("masterPrompt", "تحريك $imageDescription مع ديناميكيات حركة واقعية بأسلوب $physicsStyle وحركة كاميرا $cameraMotion، دقة 8k وتناسق فائق للألوان والملامح.")
            val scenes = JSONArray().apply {
                put(JSONObject().apply {
                    put("timeRange", "0:00 - 0:05")
                    put("visualPrompt", "تبدأ الصورة بالنبض بالحياة: ينساب الماء أو الهواء بمرونة فائقة ($physicsStyle) مع تقريب بطيء تدريجي ($cameraMotion) نحو المركز.")
                    put("audioVO", "موسيقى تصويرية غامرة ترسم عمق المشهد وحيويته.")
                    put("textOnScreen", "رسوم متحركة سينمائية فائقة الذكاء")
                })
            }
            json.put("scenes", scenes)
            json.put("callToAction", "شارك هذا الفيديو الرائع الآن أو قم بتحميله بجودة كاملة مجاناً!")
        } else {
            json.put("title", "Animate: $imageDescription ($errorNote)")
            json.put("estimatedDuration", "5 Seconds")
            json.put("masterPrompt", "Animate $imageDescription with ultra-realistic fluid physics matching $physicsStyle, combined with smooth $cameraMotion camera work, 8k resolution, keeping perfect element consistency.")
            val scenes = JSONArray().apply {
                put(JSONObject().apply {
                    put("timeRange", "0:00 - 0:05")
                    put("visualPrompt", "The static image seamlessly transitions into movement. Water/particles flow gracefully following $physicsStyle, with a cinematic $cameraMotion panning the subject.")
                    put("audioVO", "Cinematic atmospheric sound design adding dramatic depth.")
                    put("textOnScreen", "Animated with Next-Gen AI")
                })
            }
            json.put("scenes", scenes)
            json.put("callToAction", "Download your animated masterpiece now and share it with the world!")
        }
        return json.toString()
    }
}
