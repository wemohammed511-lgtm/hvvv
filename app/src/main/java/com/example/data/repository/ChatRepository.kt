package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.remote.AiProviderType
import com.example.data.remote.UniversalAiClient
import com.example.engine.OfflineAiEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val chatDao = database.chatDao()
    private val prefs = context.getSharedPreferences("ms_almohtal_prefs", Context.MODE_PRIVATE)

    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    fun getSelectedProvider(): AiProviderType {
        val id = prefs.getString("selected_ai_provider", AiProviderType.GROQ.id) ?: AiProviderType.GROQ.id
        return AiProviderType.fromId(id)
    }

    fun setSelectedProvider(provider: AiProviderType) {
        prefs.edit().putString("selected_ai_provider", provider.id).apply()
    }

    fun getSelectedModel(provider: AiProviderType): String {
        return prefs.getString("selected_model_${provider.id}", provider.defaultModel) ?: provider.defaultModel
    }

    fun setSelectedModel(provider: AiProviderType, model: String) {
        prefs.edit().putString("selected_model_${provider.id}", model.trim()).apply()
    }

    fun getCustomApiKey(): String {
        return prefs.getString("custom_api_key", "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
    }

    fun isForceOffline(): Boolean {
        return prefs.getBoolean("force_offline_mode", false)
    }

    fun setForceOffline(force: Boolean) {
        prefs.edit().putBoolean("force_offline_mode", force).apply()
    }

    fun getAiPersonality(): String {
        return prefs.getString("ai_personality", "مساعد عام ذكي ومتجاوب") ?: "مساعد عام ذكي ومتجاوب"
    }

    fun setAiPersonality(personality: String) {
        prefs.edit().putString("ai_personality", personality).apply()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun createNewSession(initialTitle: String = "محادثة جديدة"): ChatSessionEntity =
        withContext(Dispatchers.IO) {
            val session = ChatSessionEntity(
                id = UUID.randomUUID().toString(),
                title = initialTitle,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            chatDao.insertSession(session)
            session
        }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) =
        withContext(Dispatchers.IO) {
            val session = chatDao.getSessionById(sessionId)
            if (session != null) {
                chatDao.updateSession(session.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
            }
        }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun saveUserMessage(sessionId: String, text: String): ChatMessageEntity =
        withContext(Dispatchers.IO) {
            val msg = ChatMessageEntity(
                sessionId = sessionId,
                sender = "user",
                content = text,
                timestamp = System.currentTimeMillis(),
                isOffline = false
            )
            val id = chatDao.insertMessage(msg)
            val session = chatDao.getSessionById(sessionId)
            if (session != null) {
                val updatedTitle = if (session.title == "محادثة جديدة" && text.isNotBlank()) {
                    if (text.length > 30) text.take(30) + "..." else text
                } else session.title
                chatDao.updateSession(session.copy(title = updatedTitle, updatedAt = System.currentTimeMillis()))
            }
            msg.copy(id = id)
        }

    suspend fun getAiResponse(sessionId: String, userPrompt: String): ChatMessageEntity =
        withContext(Dispatchers.IO) {
            val forceOffline = isForceOffline()
            val hasNet = isNetworkAvailable()
            val currentProvider = getSelectedProvider()
            val currentModel = getSelectedModel(currentProvider)
            val customKey = getCustomApiKey()

            // If forced offline or no internet: use offline engine directly
            if (forceOffline || !hasNet) {
                val offlineResult = OfflineAiEngine.generateResponse(userPrompt)
                val aiMsg = ChatMessageEntity(
                    sessionId = sessionId,
                    sender = "ms_almohtal",
                    content = offlineResult.text,
                    timestamp = System.currentTimeMillis(),
                    isOffline = true,
                    modelUsed = "MS Offline Engine (بدون نت)"
                )
                val id = chatDao.insertMessage(aiMsg)
                return@withContext aiMsg.copy(id = id)
            }

            // Online generation using UniversalAiClient
            val systemPrompt = "أنت MS Almohtal، مساعد ذكاء اصطناعي عربي فائق الذكاء، ودود ومحترف. شخصيتك الحالية: ${getAiPersonality()}."
            val result = UniversalAiClient.generateChatCompletion(
                provider = currentProvider,
                modelName = currentModel,
                prompt = userPrompt,
                systemInstruction = systemPrompt,
                apiKey = customKey
            )

            if (result.success && result.text.isNotBlank()) {
                val aiMsg = ChatMessageEntity(
                    sessionId = sessionId,
                    sender = "ms_almohtal",
                    content = result.text,
                    timestamp = System.currentTimeMillis(),
                    isOffline = false,
                    modelUsed = "${result.providerName} • ${result.modelUsed}"
                )
                val id = chatDao.insertMessage(aiMsg)
                aiMsg.copy(id = id)
            } else {
                // Fallback to offline engine
                val offlineFallback = OfflineAiEngine.generateResponse(userPrompt)
                val notice = """
                    ⚠️ *(تعذر الاتصال بـ ${result.providerName}: ${result.error ?: "خطأ غير متوقع"}. تم تشغيل المحرك الذكي المحلي تلقائياً)*
                    
                    ${offlineFallback.text}
                """.trimIndent()
                val aiMsg = ChatMessageEntity(
                    sessionId = sessionId,
                    sender = "ms_almohtal",
                    content = notice,
                    timestamp = System.currentTimeMillis(),
                    isOffline = true,
                    modelUsed = "MS Fallback Engine"
                )
                val id = chatDao.insertMessage(aiMsg)
                aiMsg.copy(id = id)
            }
        }
}
