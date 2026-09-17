package com.example.data.remote

enum class AiProviderType(
    val id: String,
    val displayNameArabic: String,
    val baseUrl: String,
    val modelsUrl: String,
    val defaultModel: String,
    val requiresApiKey: Boolean
) {
    GROQ(
        id = "groq",
        displayNameArabic = "جروك (Groq)",
        baseUrl = "https://g4f.space/api/groq/",
        modelsUrl = "https://g4f.space/api/groq/models",
        defaultModel = "llama-3.3-70b-versatile",
        requiresApiKey = false
    ),
    OLLAMA(
        id = "ollama",
        displayNameArabic = "أولاما (Ollama)",
        baseUrl = "https://g4f.space/api/ollama/",
        modelsUrl = "https://g4f.space/api/ollama/models",
        defaultModel = "llama3:latest",
        requiresApiKey = false
    ),
    POLLINATIONS(
        id = "pollinations",
        displayNameArabic = "Pollinations.ai",
        baseUrl = "https://g4f.space/api/pollinations/",
        modelsUrl = "https://g4f.space/api/pollinations/models",
        defaultModel = "openai",
        requiresApiKey = false
    ),
    NVIDIA(
        id = "nvidia",
        displayNameArabic = "إنفيديا (NVIDIA)",
        baseUrl = "https://g4f.space/api/nvidia/",
        modelsUrl = "https://g4f.space/api/nvidia/models",
        defaultModel = "meta/llama-3.1-70b-instruct",
        requiresApiKey = false
    ),
    G4F_GEMINI(
        id = "gemini_g4f",
        displayNameArabic = "جماياناي (Gemini G4F)",
        baseUrl = "https://g4f.space/api/gemini/",
        modelsUrl = "https://g4f.space/api/gemini/models",
        defaultModel = "gemini-2.0-flash",
        requiresApiKey = false
    ),
    G4F_V1(
        id = "g4f_v1",
        displayNameArabic = "باجوهات (G4F v1)",
        baseUrl = "https://g4f.space/v1/",
        modelsUrl = "https://g4f.space/v1/models",
        defaultModel = "gpt-4o",
        requiresApiKey = true
    ),
    GOOGLE_DIRECT(
        id = "google_direct",
        displayNameArabic = "Google Gemini الرسمي",
        baseUrl = "https://generativelanguage.googleapis.com/",
        modelsUrl = "",
        defaultModel = "gemini-3.5-flash",
        requiresApiKey = false
    );

    companion object {
        fun fromId(id: String): AiProviderType {
            return entries.firstOrNull { it.id == id } ?: GROQ
        }
    }
}
