package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelName: String = "MS Almohtal AI"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val sender: String, // "user" or "ms_almohtal"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOffline: Boolean = false,
    val modelUsed: String = "Gemini 3.5 Flash"
)
