package com.example.footballstats.ui.profile

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
    val model: ProfileViewModel = viewModel(factory = FootballDependencies.viewModelFactory)
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
    ProfileScreen(state, onEdit, model::downloadResume, model::loadProfile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(state: ProfileUiState, onEdit: () -> Unit, onResume: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = onEdit, enabled = !state.isLoading) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(state.profile.avatarUri)
                Text(
                    state.profile.fullName.ifBlank { "Ваш профиль" },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                if (state.profile.position.isNotBlank()) {
                    Text(state.profile.position, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
                if (state.profile.fullName.isBlank() && state.profile.resumeUrl.isBlank()) {
                    Text("Добавьте имя, фото и ссылку на резюме", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = onEdit) { Text("Заполнить профиль") }
                }
                Button(onClick = onResume, enabled = state.profile.resumeUrl.isNotBlank() && !state.isDownloading) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.isDownloading) "Скачиваем…" else "Резюме")
                }
                if (state.profile.resumeUrl.isBlank()) {
                    Text("Ссылка на резюме пока не добавлена", style = MaterialTheme.typography.bodySmall)
                }
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Обновить профиль") }
                }
            }
        }
    }
}
