package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double? = 0.7,
    val stream: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    val message: OpenAiMessage? = null,
    val delta: OpenAiMessage? = null,
    val finish_reason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)
