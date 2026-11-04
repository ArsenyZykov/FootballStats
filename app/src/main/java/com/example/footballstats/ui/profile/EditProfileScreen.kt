package com.example.footballstats.ui.profile

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.di.FootballDependencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileRoute(onClose: () -> Unit) {
    val model: EditProfileViewModel = viewModel(factory = FootballDependencies.viewModelFactory)
    val state by model.uiState.collectAsState()
    var showPhotoSource by rememberSaveable { mutableStateOf(false) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        model.selectPhoto(uri?.toString())
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), model::cameraResult)
    LaunchedEffect(state.cameraLaunchUri) {
        state.cameraLaunchUri?.let { uri ->
            model.cameraLaunched()
            try {
                camera.launch(uri.toUri())
            } catch (_: ActivityNotFoundException) {
                model.cameraUnavailable()
            } catch (_: SecurityException) {
                model.cameraUnavailable()
            }
        }
    }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onClose() }
    BackHandler { model.cancel() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = model::cancel, enabled = !state.isBusy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад без сохранения")
                    }
                },
                actions = { TextButton(onClick = model::save, enabled = state.canEdit) { Text("Готово") } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isReady) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileAvatar(state.draft.avatarUri, Modifier.clickable(enabled = state.canEdit, onClickLabel = "Изменить фото") { showPhotoSource = true })
                        TextButton(onClick = { showPhotoSource = true }, enabled = state.canEdit) { Text("Изменить фото") }
                    }
                    OutlinedTextField(
                        value = state.draft.fullName, onValueChange = model::changeName,
                        modifier = Modifier.fillMaxWidth(), label = { Text("ФИО") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true, enabled = state.canEdit
                    )
                    OutlinedTextField(
                        value = state.draft.position, onValueChange = model::changePosition,
                        modifier = Modifier.fillMaxWidth(), label = { Text("Должность") },
                        singleLine = true, enabled = state.canEdit
                    )
                    OutlinedTextField(
                        value = state.draft.resumeUrl, onValueChange = model::changeResume,
                        modifier = Modifier.fillMaxWidth(), label = { Text("Ссылка на резюме") },
                        placeholder = { Text("https://example.com/resume.pdf") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true, enabled = state.canEdit,
                        isError = state.urlError != null,
                        supportingText = { Text(state.urlError ?: "Прямая HTTPS-ссылка на PDF или другой документ, до 25 МБ") }
                    )
                    if (state.isBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (!state.isReady) Button(onClick = model::loadProfile) { Text("Повторить") }
            }
        }
    }
    if (showPhotoSource) {
        AlertDialog(
            onDismissRequest = { showPhotoSource = false },
            title = { Text("Фото профиля") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showPhotoSource = false
                            try {
                                gallery.launch(arrayOf("image/*"))
                            } catch (_: ActivityNotFoundException) {
                                model.externalAppUnavailable("На устройстве нет приложения для выбора фото.")
                            } catch (_: SecurityException) {
                                model.externalAppUnavailable("Не удалось открыть выбранное фото.")
                            }
                        }
                    ) { Text("Галерея") }
                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = { showPhotoSource = false; model.prepareCamera() }) { Text("Камера") }
                }
            },
            confirmButton = { TextButton(onClick = { showPhotoSource = false }) { Text("Отмена") } }
        )
    }
}
