package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AiProviderType
import com.example.ui.theme.AlmohtalBorder
import com.example.ui.theme.AlmohtalCyan
import com.example.ui.theme.AlmohtalOfflineAmber
import com.example.ui.theme.AlmohtalPurple
import com.example.ui.theme.AlmohtalSurface
import com.example.ui.theme.AlmohtalSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentApiKey: String,
    isForceOffline: Boolean,
    currentPersonality: String,
    selectedProvider: AiProviderType,
    selectedModel: String,
    availableModels: List<String>,
    isLoadingModels: Boolean,
    onSelectProvider: (AiProviderType) -> Unit,
    onSelectModel: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onToggleOffline: (Boolean) -> Unit,
    onSavePersonality: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentApiKey) }
    var selectedPersonality by remember { mutableStateOf(currentPersonality) }
    var customModelInput by remember { mutableStateOf(selectedModel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AlmohtalCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Settings",
                        tint = AlmohtalCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "إعدادات مزودي الـ AI والنظام",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Choose AI Provider (Groq, Ollama, Pollinations, NVIDIA, Gemini, v1)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = AlmohtalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اختر مزود الذكاء الاصطناعي (API Provider):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val providers = listOf(
                        AiProviderType.GROQ,
                        AiProviderType.OLLAMA,
                        AiProviderType.POLLINATIONS,
                        AiProviderType.NVIDIA,
                        AiProviderType.G4F_GEMINI,
                        AiProviderType.G4F_V1,
                        AiProviderType.GOOGLE_DIRECT
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        providers.forEach { provider ->
                            val isSelected = selectedProvider == provider
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectProvider(provider)
                                        customModelInput = provider.defaultModel
                                    }
                                    .testTag("provider_choice_${provider.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AlmohtalCyan.copy(alpha = 0.15f) else AlmohtalSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AlmohtalCyan else AlmohtalBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = provider.displayNameArabic,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSelected) AlmohtalCyan else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = provider.baseUrl,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AlmohtalCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Model Selection for selected provider
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "النموذج النشط (${selectedProvider.displayNameArabic}):",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isLoadingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AlmohtalCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Model chips from availableModels
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableModels.take(8).forEach { model ->
                            val isModelSelected = selectedModel == model
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isModelSelected) AlmohtalCyan else AlmohtalSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isModelSelected) AlmohtalCyan else AlmohtalBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        customModelInput = model
                                        onSelectModel(model)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isModelSelected) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Editable model input if user wants another specific model
                    OutlinedTextField(
                        value = customModelInput,
                        onValueChange = {
                            customModelInput = it
                            onSelectModel(it)
                        },
                        label = { Text("أو اكتب اسم النموذج يدوياً:") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_model_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlmohtalCyan,
                            unfocusedBorderColor = AlmohtalBorder
                        )
                    )
                }

                // Section 3: Force Offline Toggle
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlmohtalSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlmohtalBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isForceOffline) Icons.Default.SignalWifiOff else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isForceOffline) AlmohtalOfflineAmber else AlmohtalCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isForceOffline) "وضع أوفلاين إجباري (مفعل)" else "الوضع التلقائي الذكي",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isForceOffline) "يعمل بدون أي استهلاك للإنترنت نهائياً" else "يستخدم مزود الـ API المختار مع دعم السقوط للأوفلاين",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isForceOffline,
                            onCheckedChange = { onToggleOffline(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlmohtalOfflineAmber,
                                checkedTrackColor = AlmohtalOfflineAmber.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }
                }

                // Section 4: Custom API Key (for v1 / Google / optional)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = AlmohtalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedProvider.requiresApiKey) "مفتاح الـ API (مطلوب لـ ${selectedProvider.displayNameArabic}):"
                                   else "مفتاح الـ API (اختياري):",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        placeholder = { Text("ألصق مفتاح الـ API هنا...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlmohtalCyan,
                            unfocusedBorderColor = AlmohtalBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Section 5: AI Personality Selection
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = AlmohtalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شخصية وأسلوب MS Almohtal:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val personalities = listOf(
                        "مساعد عام ذكي وشامل",
                        "مبرمج وخبير كود محترف",
                        "مستشار أعمال وأفكار مشاريع",
                        "مدرّب لغوي وكاتب محتوى"
                    )

                    personalities.forEach { personality ->
                        val isSelected = selectedPersonality == personality
                        OutlinedButton(
                            onClick = { selectedPersonality = personality },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) AlmohtalCyan.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AlmohtalCyan else AlmohtalBorder
                            )
                        ) {
                            Text(
                                text = personality,
                                color = if (isSelected) AlmohtalCyan else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveApiKey(apiKeyText)
                    onSavePersonality(selectedPersonality)
                    onSelectModel(customModelInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlmohtalCyan),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("حفظ وتطبيق", color = Color(0xFF00363D))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_settings_button")
            ) {
                Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
