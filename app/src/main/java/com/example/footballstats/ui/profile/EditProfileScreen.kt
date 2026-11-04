package com.example.footballstats.ui.profile

import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.di.FootballDependencies
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileRoute(onClose: () -> Unit) {
    val model: EditProfileViewModel = viewModel(
        factory = FootballDependencies.viewModelFactory
    )
    val state by model.uiState.collectAsState()

    val context = LocalContext.current
    val reminderScheduler = FootballDependencies.lessonReminderScheduler

    var showPhotoSource by rememberSaveable {
        mutableStateOf(false)
    }
    var showNotificationSettings by rememberSaveable {
        mutableStateOf(false)
    }
    var showExactAlarmSettings by rememberSaveable {
        mutableStateOf(false)
    }

    fun continueSaving() {
        if (!model.uiState.value.canSave) return

        when {
            !reminderScheduler.areNotificationsEnabled() ->
                showNotificationSettings = true

            !reminderScheduler.canScheduleExactAlarms() ->
                showExactAlarmSettings = true

            else -> model.save()
        }
    }

    fun openSettings(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            model.externalAppUnavailable(
                "Не удалось открыть настройки. " +
                        "Откройте настройки FootballStats вручную."
            )
        } catch (_: SecurityException) {
            model.externalAppUnavailable(
                "Android не разрешил открыть настройки приложения."
            )
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            continueSaving()
        } else {
            showNotificationSettings = true
        }
    }

    val gallery = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        model.selectPhoto(uri?.toString())
    }

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
        model::cameraResult
    )

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

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onClose()
        }
    }

    BackHandler {
        model.cancel()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text("Редактирование")
                },
                navigationIcon = {
                    IconButton(
                        onClick = model::cancel,
                        enabled = !state.isBusy
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад без сохранения"
                        )
                    }
                },
                actions = {
                    TextButton(
                        enabled = state.canSave,
                        onClick = {
                            if (
                                Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermission.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                continueSaving()
                            }
                        }
                    ) {
                        Text("Готово")
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
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isReady) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileAvatar(
                            state.draft.avatarUri,
                            Modifier.clickable(
                                enabled = state.canEdit,
                                onClickLabel = "Изменить фото"
                            ) {
                                showPhotoSource = true
                            }
                        )

                        TextButton(
                            onClick = {
                                showPhotoSource = true
                            },
                            enabled = state.canEdit
                        ) {
                            Text("Изменить фото")
                        }
                    }

                    OutlinedTextField(
                        value = state.draft.fullName,
                        onValueChange = model::changeName,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("ФИО")
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        singleLine = true,
                        enabled = state.canEdit
                    )

                    OutlinedTextField(
                        value = state.draft.position,
                        onValueChange = model::changePosition,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Должность")
                        },
                        singleLine = true,
                        enabled = state.canEdit
                    )

                    OutlinedTextField(
                        value = state.draft.resumeUrl,
                        onValueChange = model::changeResume,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Ссылка на резюме")
                        },
                        placeholder = {
                            Text("https://example.com/resume.pdf")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        singleLine = true,
                        enabled = state.canEdit,
                        isError = state.urlError != null,
                        supportingText = {
                            Text(
                                state.urlError
                                    ?: "Прямая HTTPS-ссылка на PDF " +
                                    "или другой документ, до 25 МБ"
                            )
                        }
                    )

                    OutlinedTextField(
                        value = state.draft.lessonTime,
                        onValueChange = model::changeLessonTime,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Время любимой пары")
                        },
                        placeholder = {
                            Text("14:30")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        singleLine = true,
                        enabled = state.canEdit,
                        isError = state.timeError != null,
                        supportingText = {
                            Text(
                                state.timeError
                                    ?: "Формат HH:mm. Напоминание на сегодня."
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                enabled = state.canEdit,
                                onClick = {
                                    val now = Calendar.getInstance()

                                    val hour = if (state.timeError == null) {
                                        state.draft.lessonTime
                                            .substring(0, 2)
                                            .toInt()
                                    } else {
                                        now.get(Calendar.HOUR_OF_DAY)
                                    }

                                    val minute = if (state.timeError == null) {
                                        state.draft.lessonTime
                                            .substring(3, 5)
                                            .toInt()
                                    } else {
                                        now.get(Calendar.MINUTE)
                                    }

                                    TimePickerDialog(
                                        context,
                                        { _, selectedHour, selectedMinute ->
                                            model.changeLessonTime(
                                                String.format(
                                                    Locale.ROOT,
                                                    "%02d:%02d",
                                                    selectedHour,
                                                    selectedMinute
                                                )
                                            )
                                        },
                                        hour,
                                        minute,
                                        true
                                    ).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Выбрать время"
                                )
                            }
                        }
                    )

                    Text(
                        text = "Выберите время позже текущего. " +
                                "При сохранении будет установлено одно " +
                                "напоминание на сегодня.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (state.isBusy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!state.isReady) {
                    Button(onClick = model::loadProfile) {
                        Text("Повторить")
                    }
                }
            }
        }
    }

    if (showPhotoSource) {
        AlertDialog(
            onDismissRequest = {
                showPhotoSource = false
            },
            title = {
                Text("Фото профиля")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showPhotoSource = false

                            try {
                                gallery.launch(arrayOf("image/*"))
                            } catch (_: ActivityNotFoundException) {
                                model.externalAppUnavailable(
                                    "На устройстве нет приложения " +
                                            "для выбора фото."
                                )
                            } catch (_: SecurityException) {
                                model.externalAppUnavailable(
                                    "Не удалось открыть выбранное фото."
                                )
                            }
                        }
                    ) {
                        Text("Галерея")
                    }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showPhotoSource = false
                            model.prepareCamera()
                        }
                    ) {
                        Text("Камера")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoSource = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showNotificationSettings) {
        AlertDialog(
            onDismissRequest = {
                showNotificationSettings = false
            },
            title = {
                Text("Разрешите уведомления")
            },
            text = {
                Text(
                    "Для напоминания включите уведомления FootballStats " +
                            "и канал «Напоминания о паре». " +
                            "После этого вернитесь в приложение " +
                            "и снова нажмите «Готово»."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationSettings = false

                        val intent = if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ) {
                            Intent(
                                Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            ).putExtra(
                                Settings.EXTRA_APP_PACKAGE,
                                context.packageName
                            )
                        } else {
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${context.packageName}".toUri()
                            )
                        }

                        openSettings(intent)
                    }
                ) {
                    Text("Настройки")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationSettings = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showExactAlarmSettings) {
        AlertDialog(
            onDismissRequest = {
                showExactAlarmSettings = false
            },
            title = {
                Text("Точное время напоминания")
            },
            text = {
                Text(
                    "Разрешите приложению устанавливать точные " +
                            "будильники и напоминания. После этого " +
                            "вернитесь сюда и снова нажмите «Готово»."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExactAlarmSettings = false

                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ) {
                            openSettings(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    "package:${context.packageName}".toUri()
                                )
                            )
                        }
                    }
                ) {
                    Text("Настройки")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExactAlarmSettings = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}