package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.remote.AiProviderType
import com.example.data.remote.UniversalAiClient
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputMessage = MutableStateFlow("")
    val inputMessage: StateFlow<String> = _inputMessage.asStateFlow()

    private val _isForceOffline = MutableStateFlow(repository.isForceOffline())
    val isForceOffline: StateFlow<Boolean> = _isForceOffline.asStateFlow()

    private val _customApiKey = MutableStateFlow(repository.getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _aiPersonality = MutableStateFlow(repository.getAiPersonality())
    val aiPersonality: StateFlow<String> = _aiPersonality.asStateFlow()

    private val _selectedProvider = MutableStateFlow(repository.getSelectedProvider())
    val selectedProvider: StateFlow<AiProviderType> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow(repository.getSelectedModel(_selectedProvider.value))
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(listOf(_selectedProvider.value.defaultModel))
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<ChatMessageEntity>> = _activeSessionId.flatMapLatest { id ->
        if (id == null) {
            MutableStateFlow(emptyList())
        } else {
            repository.getMessagesForSession(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Pick latest session or create a fresh one
            repository.getAllSessions().collect { list ->
                if (_activeSessionId.value == null) {
                    if (list.isNotEmpty()) {
                        _activeSessionId.value = list.first().id
                    } else {
                        val newSession = repository.createNewSession()
                        _activeSessionId.value = newSession.id
                    }
                }
            }
        }
        // Fetch models for initial provider
        fetchModelsForCurrentProvider()
    }

    fun onInputChange(newText: String) {
        _inputMessage.value = newText
    }

    fun sendMessage(customText: String? = null) {
        val textToSend = (customText ?: _inputMessage.value).trim()
        if (textToSend.isBlank() || _isLoading.value) return

        val currentSessionId = _activeSessionId.value ?: return

        _inputMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.saveUserMessage(currentSessionId, textToSend)
                repository.getAiResponse(currentSessionId, textToSend)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newSession = repository.createNewSession()
            _activeSessionId.value = newSession.id
            _showHistorySheet.value = false
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        _showHistorySheet.value = false
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
            }
        }
    }

    fun toggleForceOffline(enabled: Boolean) {
        _isForceOffline.value = enabled
        repository.setForceOffline(enabled)
    }

    fun updateCustomApiKey(key: String) {
        _customApiKey.value = key
        repository.setCustomApiKey(key)
    }

    fun updateAiPersonality(personality: String) {
        _aiPersonality.value = personality
        repository.setAiPersonality(personality)
    }

    fun selectProvider(provider: AiProviderType) {
        _selectedProvider.value = provider
        repository.setSelectedProvider(provider)
        val savedModel = repository.getSelectedModel(provider)
        _selectedModel.value = savedModel
        fetchModelsForCurrentProvider()
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
        repository.setSelectedModel(_selectedProvider.value, model)
    }

    private fun fetchModelsForCurrentProvider() {
        val provider = _selectedProvider.value
        _isLoadingModels.value = true
        viewModelScope.launch {
            try {
                val list = UniversalAiClient.fetchModelsForProvider(provider)
                _availableModels.value = list
                if (_selectedModel.value.isBlank() || !list.contains(_selectedModel.value)) {
                    val default = list.firstOrNull() ?: provider.defaultModel
                    _selectedModel.value = default
                    repository.setSelectedModel(provider, default)
                }
            } finally {
                _isLoadingModels.value = false
            }
        }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowHistorySheet(show: Boolean) {
        _showHistorySheet.value = show
    }

    fun isOnlineReady(): Boolean {
        return repository.isNetworkAvailable() && !_isForceOffline.value
    }
}
