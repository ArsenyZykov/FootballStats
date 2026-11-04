package com.example.footballstats.ui.profile

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.di.FootballDependencies

@Composable
fun ProfileRoute(onEdit: () -> Unit) {
    val model: ProfileViewModel = viewModel(
        factory = FootballDependencies.viewModelFactory
    )
    val state by model.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.document) {
        state.document?.let { document ->
            model.documentOpened()

            val uri = document.uri.toUri()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, document.mimeType)
                clipData = ClipData.newRawUri("Резюме", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                model.viewerUnavailable()
            } catch (_: SecurityException) {
                model.viewerUnavailable()
            }
        }
    }

    ProfileScreen(
        state = state,
        onEdit = onEdit,
        onResume = model::downloadResume,
        onRetry = model::loadProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text("Профиль")
                },
                actions = {
                    IconButton(
                        onClick = onEdit,
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать профиль"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(state.profile.avatarUri)

                Text(
                    text = state.profile.fullName.ifBlank {
                        "Ваш профиль"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                if (state.profile.position.isNotBlank()) {
                    Text(
                        text = state.profile.position,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                if (
                    state.profile.fullName.isBlank() &&
                    state.profile.resumeUrl.isBlank()
                ) {
                    Text(
                        text = "Добавьте имя, фото и ссылку на резюме",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(onClick = onEdit) {
                        Text("Заполнить профиль")
                    }
                }

                Button(
                    onClick = onResume,
                    enabled = state.profile.resumeUrl.isNotBlank() &&
                            !state.isDownloading
                ) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        if (state.isDownloading) {
                            "Скачиваем…"
                        } else {
                            "Резюме"
                        }
                    )
                }

                if (state.profile.resumeUrl.isBlank()) {
                    Text(
                        text = "Ссылка на резюме пока не добавлена",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                LessonTimeCard(
                    lessonTime = state.profile.lessonTime
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    TextButton(onClick = onRetry) {
                        Text("Обновить профиль")
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonTimeCard(lessonTime: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Время любимой пары",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = lessonTime.ifBlank {
                        "Время не указано"
                    },
                    style = if (lessonTime.isBlank()) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}