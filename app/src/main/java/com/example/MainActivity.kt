package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ChatViewModel
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatTopBar
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.MessageBubble
import com.example.ui.components.SettingsDialog
import com.example.ui.components.WelcomeHeroCard
import com.example.ui.theme.AlmohtalDarkNavy
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val inputMessage by viewModel.inputMessage.collectAsStateWithLifecycle()
    val isForceOffline by viewModel.isForceOffline.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val aiPersonality by viewModel.aiPersonality.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showHistorySheet by viewModel.showHistorySheet.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val isLoadingModels by viewModel.isLoadingModels.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages or loading
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            ChatTopBar(
                isOnline = viewModel.isOnlineReady(),
                isForceOffline = isForceOffline,
                selectedProvider = selectedProvider,
                onToggleOffline = { viewModel.toggleForceOffline(!isForceOffline) },
                onOpenHistory = { viewModel.setShowHistorySheet(true) },
                onOpenSettings = { viewModel.setShowSettingsDialog(true) }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputMessage,
                isLoading = isLoading,
                onTextChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.sendMessage() }
            )
        },
        containerColor = AlmohtalDarkNavy
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AlmohtalDarkNavy)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Show Welcome Hero Card at top if messages are empty or few
                if (messages.isEmpty()) {
                    item {
                        WelcomeHeroCard(
                            isOffline = isForceOffline || !viewModel.isOnlineReady(),
                            selectedProvider = selectedProvider,
                            onSuggestionClick = { suggestion ->
                                viewModel.sendMessage(suggestion)
                            }
                        )
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(message = msg)
                }
            }
        }
    }

    // Settings & API Key Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentApiKey = customApiKey,
            isForceOffline = isForceOffline,
            currentPersonality = aiPersonality,
            selectedProvider = selectedProvider,
            selectedModel = selectedModel,
            availableModels = availableModels,
            isLoadingModels = isLoadingModels,
            onSelectProvider = { viewModel.selectProvider(it) },
            onSelectModel = { viewModel.selectModel(it) },
            onSaveApiKey = { viewModel.updateCustomApiKey(it) },
            onToggleOffline = { viewModel.toggleForceOffline(it) },
            onSavePersonality = { viewModel.updateAiPersonality(it) },
            onDismiss = { viewModel.setShowSettingsDialog(false) }
        )
    }

    // Chat History Bottom Sheet
    if (showHistorySheet) {
        HistoryBottomSheet(
            sessions = sessions,
            activeSessionId = activeSessionId,
            onSelectSession = { viewModel.selectSession(it) },
            onDeleteSession = { viewModel.deleteSession(it) },
            onNewChat = { viewModel.createNewChat() },
            onDismiss = { viewModel.setShowHistorySheet(false) }
        )
    }
}
