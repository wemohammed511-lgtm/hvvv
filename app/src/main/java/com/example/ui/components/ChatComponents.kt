package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.AlmohtalBorder
import com.example.ui.theme.AlmohtalCard
import com.example.ui.theme.AlmohtalCyan
import com.example.ui.theme.AlmohtalDarkNavy
import com.example.ui.theme.AlmohtalOfflineAmber
import com.example.ui.theme.AlmohtalOnlineGreen
import com.example.ui.theme.AlmohtalPurple
import com.example.ui.theme.AlmohtalSurface
import com.example.ui.theme.AlmohtalSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.remote.AiProviderType

@Composable
fun ChatTopBar(
    isOnline: Boolean,
    isForceOffline: Boolean,
    selectedProvider: AiProviderType,
    onToggleOffline: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = AlmohtalDarkNavy,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Avatar + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AlmohtalCyan, AlmohtalPurple)
                            )
                        )
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ms_almohtal_logo),
                        contentDescription = "MS Almohtal Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "MS Almohtal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isForceOffline || !isOnline) "المحرك الذكي المحلي" else selectedProvider.displayNameArabic,
                        style = MaterialTheme.typography.bodySmall,
                        color = AlmohtalCyan.copy(alpha = 0.8f)
                    )
                }
            }

            // Mode Pill & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Online/Offline Status Pill
                val isEffectivelyOffline = isForceOffline || !isOnline
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isEffectivelyOffline) AlmohtalOfflineAmber.copy(alpha = 0.15f)
                            else AlmohtalOnlineGreen.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isEffectivelyOffline) AlmohtalOfflineAmber.copy(alpha = 0.5f)
                            else AlmohtalOnlineGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("mode_toggle_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isEffectivelyOffline) AlmohtalOfflineAmber else AlmohtalOnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEffectivelyOffline) "أوفلاين (بدون نت)" else selectedProvider.id.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isEffectivelyOffline) AlmohtalOfflineAmber else AlmohtalOnlineGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // History Button
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.testTag("history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Chat History",
                        tint = Color.White
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeHeroCard(
    isOffline: Boolean,
    selectedProvider: AiProviderType,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlmohtalSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AlmohtalBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AlmohtalCyan.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ms_almohtal_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "مرحباً بك في MS Almohtal AI",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "تطبيق ذكاء اصطناعي شامل يدعم جميع مزودي الـ API (جروك، أولاما، إنفيديا، Pollinations، جماياناي، وباجوهات v1)، مع محرك محلي يعمل بدون إنترنت.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AlmohtalSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.SignalWifiOff else Icons.Default.Cloud,
                    contentDescription = null,
                    tint = if (isOffline) AlmohtalOfflineAmber else AlmohtalCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOffline) "الوضع الحالي: المحرك الذكي المحلي (بدون نت)" else "المزود النشط: ${selectedProvider.displayNameArabic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOffline) AlmohtalOfflineAmber else AlmohtalCyan
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "جرّب أحد الأسئلة السريعة:",
                style = MaterialTheme.typography.labelMedium,
                color = AlmohtalCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            val suggestions = listOf(
                "⚡ هل يمكنك العمل بدون نت؟",
                "🧮 احسب: 250 * 18",
                "💻 اكتب كود بايثون بسيط",
                "🚀 اقترح أفكار مشاريع ذكاء اصطناعي",
                "✉️ نموذج إيميل رسمي احترافي",
                "🤖 ما هو الذكاء الاصطناعي؟"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AlmohtalCard)
                            .border(1.dp, AlmohtalBorder, RoundedCornerShape(16.dp))
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AlmohtalCyan.copy(alpha = 0.2f))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "MS Almohtal",
                    tint = AlmohtalCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Model Badge for AI
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = if (message.isOffline) "⚡ ${message.modelUsed}" else "☁️ ${message.modelUsed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isOffline) AlmohtalOfflineAmber else AlmohtalCyan
                    )
                }
            }

            // Message Bubble Surface
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) {
                    AlmohtalCyan.copy(alpha = 0.15f)
                } else {
                    AlmohtalSurface
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) AlmohtalCyan.copy(alpha = 0.4f) else AlmohtalBorder
                ),
                modifier = Modifier.testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = timeFormat.format(Date(message.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (!isUser) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("MS Almohtal Response", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ النص!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = AlmohtalDarkNavy,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "اسأل MS Almohtal (أونلاين أو بدون نت)...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AlmohtalCyan,
                    unfocusedBorderColor = AlmohtalBorder,
                    focusedContainerColor = AlmohtalSurface,
                    unfocusedContainerColor = AlmohtalSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4,
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isLoading) AlmohtalCyan
                        else AlmohtalSurfaceVariant
                    )
                    .clickable(enabled = text.isNotBlank() && !isLoading) { onSend() }
                    .testTag("send_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = AlmohtalCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
