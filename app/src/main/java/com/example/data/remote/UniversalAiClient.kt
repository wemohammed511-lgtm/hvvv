package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object UniversalAiClient {
    private const val TAG = "UniversalAiClient"
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val chatRequestAdapter = moshi.adapter(OpenAiChatRequest::class.java)
    private val chatResponseAdapter = moshi.adapter(OpenAiChatResponse::class.java)

    data class GenerationResult(
        val success: Boolean,
        val text: String,
        val modelUsed: String,
        val providerName: String,
        val error: String? = null
    )

    suspend fun generateChatCompletion(
        provider: AiProviderType,
        modelName: String,
        prompt: String,
        systemInstruction: String,
        apiKey: String?
    ): GenerationResult = withContext(Dispatchers.IO) {
        if (provider == AiProviderType.GOOGLE_DIRECT) {
            return@withContext callGoogleGeminiDirect(prompt, systemInstruction, apiKey)
        }

        // Call G4F OpenAI-compatible chat completion endpoint
        val effectiveModel = if (modelName.isNotBlank()) modelName else provider.defaultModel
        val endpoint = provider.baseUrl.trimEnd('/') + "/chat/completions"

        val requestPayload = OpenAiChatRequest(
            model = effectiveModel,
            messages = listOf(
                OpenAiMessage(role = "system", content = systemInstruction),
                OpenAiMessage(role = "user", content = prompt)
            ),
            temperature = 0.7,
            stream = false
        )

        val jsonString = chatRequestAdapter.toJson(requestPayload)
        val requestBody = jsonString.toRequestBody(JSON_MEDIA)

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")

        val effectiveKey = apiKey?.trim().takeIf { !it.isNullOrBlank() }
        if (!effectiveKey.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $effectiveKey")
        }

        try {
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val code = response.code
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "API call to $endpoint returned code: $code, body: $bodyString")
                // Try parsing error message from JSON
                val errMsg = parseErrorMessage(bodyString) ?: "فشل الطلب مع الخادم (كود: $code)"
                return@withContext GenerationResult(
                    success = false,
                    text = "",
                    modelUsed = effectiveModel,
                    providerName = provider.displayNameArabic,
                    error = errMsg
                )
            }

            // Parse response
            val parsedResponse = try {
                chatResponseAdapter.fromJson(bodyString)
            } catch (e: Exception) {
                null
            }

            val content = parsedResponse?.choices?.firstOrNull()?.message?.content
                ?: parsedResponse?.choices?.firstOrNull()?.delta?.content
                ?: tryFallbackContentExtraction(bodyString)

            if (!content.isNullOrBlank()) {
                GenerationResult(
                    success = true,
                    text = content.trim(),
                    modelUsed = effectiveModel,
                    providerName = provider.displayNameArabic
                )
            } else {
                GenerationResult(
                    success = false,
                    text = "",
                    modelUsed = effectiveModel,
                    providerName = provider.displayNameArabic,
                    error = "لم يرجع الخادم أي نص صالح للمحادثة."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during call to $endpoint", e)
            GenerationResult(
                success = false,
                text = "",
                modelUsed = effectiveModel,
                providerName = provider.displayNameArabic,
                error = e.localizedMessage ?: "حدث خطأ أثناء الاتصال بالإنترنت"
            )
        }
    }

    private suspend fun callGoogleGeminiDirect(
        prompt: String,
        systemInstruction: String,
        apiKey: String?
    ): GenerationResult {
        val effectiveKey = GeminiApiClient.resolveEffectiveKey(apiKey)
        if (effectiveKey.isNullOrBlank()) {
            return GenerationResult(
                success = false,
                text = "",
                modelUsed = "gemini-3.5-flash",
                providerName = "Google Gemini",
                error = "يرجى إدخال مفتاح Gemini API في الإعدادات."
            )
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstruction))
                )
            )

            val response = GeminiApiClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = effectiveKey,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.error?.message
                ?: "لم يتلق التطبيق رداً من Gemini"

            GenerationResult(
                success = true,
                text = text,
                modelUsed = "gemini-3.5-flash",
                providerName = "Google Gemini"
            )
        } catch (e: Exception) {
            GenerationResult(
                success = false,
                text = "",
                modelUsed = "gemini-3.5-flash",
                providerName = "Google Gemini",
                error = e.localizedMessage ?: "فشل الاتصال بـ Gemini API"
            )
        }
    }

    suspend fun fetchModelsForProvider(provider: AiProviderType): List<String> = withContext(Dispatchers.IO) {
        if (provider.modelsUrl.isBlank()) {
            return@withContext listOf(provider.defaultModel)
        }

        try {
            val request = Request.Builder()
                .url(provider.modelsUrl)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext listOf(provider.defaultModel)
            }

            val body = response.body?.string() ?: return@withContext listOf(provider.defaultModel)
            val modelList = mutableListOf<String>()

            // JSON can be array of strings, array of objects, or { "data": [...] }
            val trimmed = body.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val item = array.get(i)
                    if (item is String) {
                        modelList.add(item)
                    } else if (item is JSONObject) {
                        val id = item.optString("id").ifBlank { item.optString("name") }
                        if (id.isNotBlank()) modelList.add(id)
                    }
                }
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                val dataArr = obj.optJSONArray("data") ?: obj.optJSONArray("models")
                if (dataArr != null) {
                    for (i in 0 until dataArr.length()) {
                        val item = dataArr.get(i)
                        if (item is String) {
                            modelList.add(item)
                        } else if (item is JSONObject) {
                            val id = item.optString("id").ifBlank { item.optString("name") }
                            if (id.isNotBlank()) modelList.add(id)
                        }
                    }
                }
            }

            if (modelList.isNotEmpty()) {
                modelList.distinct()
            } else {
                listOf(provider.defaultModel)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch models for ${provider.name}: ${e.message}")
            listOf(provider.defaultModel)
        }
    }

    private fun parseErrorMessage(json: String): String? {
        return try {
            val obj = JSONObject(json)
            val err = obj.optJSONObject("error")
            err?.optString("message") ?: obj.optString("message").ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun tryFallbackContentExtraction(json: String): String? {
        return try {
            val obj = JSONObject(json)
            val choices = obj.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val first = choices.getJSONObject(0)
                val msg = first.optJSONObject("message")
                msg?.optString("content")?.ifBlank { null }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
