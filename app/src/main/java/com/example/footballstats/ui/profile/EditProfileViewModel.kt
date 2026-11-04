package com.example.footballstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.UserProfile
import com.example.footballstats.domain.reminder.LessonReminderScheduler
import com.example.footballstats.domain.repository.ProfileRepository
import com.example.footballstats.ui.common.launchWithErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.net.URI
import java.util.Calendar

private val lessonTimePattern = Regex(
    "([01][0-9]|2[0-3]):[0-5][0-9]"
)

data class EditProfileUiState(
    val draft: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val isBusy: Boolean = false,
    val isSaved: Boolean = false,
    val cameraLaunchUri: String? = null,
    val errorMessage: String? = null,
    val urlError: String? = null
) {
    val canEdit: Boolean
        get() = isReady && !isBusy && !isSaved

    val timeError: String?
        get() = when {
            draft.lessonTime.isBlank() ->
                "Укажите время любимой пары."

            !lessonTimePattern.matches(draft.lessonTime) ->
                "Введите время в формате HH:mm: от 00:00 до 23:59."

            else -> null
        }

    val canSave: Boolean
        get() = canEdit && timeError == null
}

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val savedState: SavedStateHandle,
    private val reminderScheduler: LessonReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Не удалось прочитать профиль."
                    )
                }
            }
        ) {
            val stored = repository.profile.first()

            val draft = if (
                savedState.get<Boolean>("hasDraft") == true
            ) {
                UserProfile(
                    fullName = savedState["name"] ?: stored.fullName,
                    position = savedState["position"] ?: stored.position,
                    avatarUri = savedState["avatar"] ?: stored.avatarUri,
                    resumeUrl = savedState["resume"] ?: stored.resumeUrl,
                    lessonTime = savedState["lessonTime"] ?: stored.lessonTime
                )
            } else {
                stored
            }

            _uiState.update {
                it.copy(
                    draft = draft,
                    isLoading = false,
                    isReady = true
                )
            }
        }
    }

    fun changeName(value: String) =
        edit { it.copy(fullName = value.take(150)) }

    fun changePosition(value: String) =
        edit { it.copy(position = value.take(150)) }

    fun changeResume(value: String) =
        edit { it.copy(resumeUrl = value.take(2048)) }

    fun changeLessonTime(value: String) =
        edit { it.copy(lessonTime = value) }

    private fun edit(transform: (UserProfile) -> UserProfile) {
        if (!_uiState.value.canEdit) return

        updateDraft(transform(_uiState.value.draft))
    }

    private fun updateDraft(profile: UserProfile) {
        savedState["hasDraft"] = true
        savedState["name"] = profile.fullName
        savedState["position"] = profile.position
        savedState["avatar"] = profile.avatarUri
        savedState["resume"] = profile.resumeUrl
        savedState["lessonTime"] = profile.lessonTime

        _uiState.update {
            it.copy(
                draft = profile,
                errorMessage = null,
                urlError = null
            )
        }
    }

    fun selectPhoto(uri: String?) {
        if (uri == null || !_uiState.value.canEdit) return

        importPhoto(uri, fromCamera = false)
    }

    private fun importPhoto(uri: String, fromCamera: Boolean) {
        _uiState.update {
            it.copy(
                isBusy = true,
                errorMessage = null
            )
        }

        viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessage =
                            "Не удалось загрузить фото. " +
                                    "Выберите другое изображение (до 25 МБ)."
                    )
                }
            }
        ) {
            try {
                val avatar = repository.importAvatar(uri)
                val previous = _uiState.value.draft.avatarUri

                updateDraft(
                    _uiState.value.draft.copy(avatarUri = avatar)
                )

                if (previous != avatar) {
                    repository.discardPhoto(previous)
                }
            } finally {
                if (fromCamera) {
                    repository.discardPhoto(uri)
                }
            }

            _uiState.update {
                it.copy(isBusy = false)
            }
        }
    }

    fun prepareCamera() {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(
                isBusy = true,
                errorMessage = null
            )
        }

        viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessage =
                            "Не удалось подготовить файл для камеры."
                    )
                }
            }
        ) {
            val uri = repository.createCameraPhoto()

            savedState["cameraUri"] = uri

            _uiState.update {
                it.copy(cameraLaunchUri = uri)
            }
        }
    }

    fun cameraLaunched() {
        _uiState.update {
            it.copy(cameraLaunchUri = null)
        }
    }

    fun cameraResult(success: Boolean) {
        val uri = savedState.remove<String>("cameraUri")

        _uiState.update {
            it.copy(
                cameraLaunchUri = null,
                isBusy = false
            )
        }

        if (uri != null) {
            if (success) {
                importPhoto(uri, fromCamera = true)
            } else {
                viewModelScope.launchWithErrorHandler(
                    onError = {}
                ) {
                    repository.discardPhoto(uri)
                }
            }
        }
    }

    fun cameraUnavailable() {
        cameraResult(false)

        externalAppUnavailable(
            "Не удалось открыть камеру. Выберите фото из галереи."
        )
    }

    fun externalAppUnavailable(message: String) {
        _uiState.update {
            it.copy(errorMessage = message)
        }
    }

    fun cancel() {
        if (_uiState.value.isBusy || _uiState.value.isSaved) return

        val avatar = _uiState.value.draft.avatarUri

        _uiState.update {
            it.copy(isBusy = true)
        }

        viewModelScope.launchWithErrorHandler(
            onError = {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        isSaved = true
                    )
                }
            }
        ) {
            repository.discardPhoto(avatar)

            _uiState.update {
                it.copy(
                    isBusy = false,
                    isSaved = true
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val draft = state.draft
        val profile = draft.copy(
            fullName = draft.fullName.trim(),
            position = draft.position.trim(),
            resumeUrl = draft.resumeUrl.trim()
        )

        if (
            profile.resumeUrl.isNotEmpty() &&
            !isValidResumeUrl(profile.resumeUrl)
        ) {
            _uiState.update {
                it.copy(
                    urlError =
                        "Укажите полную HTTPS-ссылку на файл, " +
                                "например https://site.ru/resume.pdf"
                )
            }
            return
        }

        val now = System.currentTimeMillis()

        val triggerAtMillis = Calendar.getInstance().apply {
            timeInMillis = now

            set(
                Calendar.HOUR_OF_DAY,
                profile.lessonTime.substring(0, 2).toInt()
            )
            set(
                Calendar.MINUTE,
                profile.lessonTime.substring(3, 5).toInt()
            )
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (triggerAtMillis <= now) {
            externalAppUnavailable(
                "Это время сегодня уже прошло. " +
                        "Выберите время позже текущего."
            )
            return
        }

        if (!reminderScheduler.areNotificationsEnabled()) {
            externalAppUnavailable(
                "Разрешите уведомления приложения " +
                        "и канал «Напоминания о паре»."
            )
            return
        }

        if (!reminderScheduler.canScheduleExactAlarms()) {
            externalAppUnavailable(
                "Разрешите точные напоминания в настройках Android."
            )
            return
        }

        _uiState.update {
            it.copy(
                isBusy = true,
                errorMessage = null,
                urlError = null
            )
        }

        var profileWasSaved = false

        viewModelScope.launchWithErrorHandler(
            onError = {
                val message = if (profileWasSaved) {
                    "Профиль сохранён, но напоминание установить " +
                            "не удалось. Проверьте разрешения, выберите " +
                            "будущее время и нажмите «Готово» ещё раз."
                } else {
                    "Не удалось сохранить профиль. Попробуйте ещё раз."
                }

                _uiState.update {
                    it.copy(
                        isBusy = false,
                        errorMessage = message
                    )
                }
            }
        ) {
            repository.save(profile)
            profileWasSaved = true

            reminderScheduler.schedule(
                fullName = profile.fullName,
                triggerAtMillis = triggerAtMillis
            )

            _uiState.update {
                it.copy(
                    draft = profile,
                    isBusy = false,
                    isSaved = true
                )
            }
        }
    }

    private fun isValidResumeUrl(value: String): Boolean =
        try {
            val uri = URI(value)

            uri.scheme.equals("https", ignoreCase = true) &&
                    !uri.host.isNullOrBlank() &&
                    uri.userInfo == null
        } catch (_: Exception) {
            false
        }
}