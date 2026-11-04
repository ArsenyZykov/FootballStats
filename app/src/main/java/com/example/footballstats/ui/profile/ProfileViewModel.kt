package com.example.footballstats.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.domain.model.ProfileDocument
import com.example.footballstats.domain.model.UserProfile
import com.example.footballstats.domain.repository.ProfileRepository
import com.example.footballstats.ui.common.launchWithErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isDownloading: Boolean = false,
    val document: ProfileDocument? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init { loadProfile() }

    fun loadProfile() {
        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeJob = viewModelScope.launchWithErrorHandler(onError = {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Не удалось прочитать профиль. Попробуйте ещё раз.") }
        }) {
            repository.profile.collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false, errorMessage = null) }
            }
        }
    }

    fun downloadResume() {
        val state = _uiState.value
        if (state.isDownloading || state.profile.resumeUrl.isBlank()) return
        _uiState.update { it.copy(isDownloading = true, errorMessage = null, document = null) }
        viewModelScope.launchWithErrorHandler(onError = { error ->
            Log.e("ProfileDownload", "Не удалось скачать резюме", error)
            val certificateDateError = generateSequence(error) { it.cause }.any {
                it is CertificateExpiredException || it is CertificateNotYetValidException
            }
            val message = if (certificateDateError) {
                "Не удалось проверить срок действия сертификата сервера. Проверьте дату и время устройства."
            } else {
                "Не удалось скачать резюме. Проверьте интернет и прямую HTTPS-ссылку на файл (до 25 МБ)."
            }
            _uiState.update { it.copy(isDownloading = false, errorMessage = message) }
        }) {
            val document = repository.downloadResume(state.profile.resumeUrl)
            _uiState.update { it.copy(isDownloading = false, document = document) }
        }
    }

    fun documentOpened() { _uiState.update { it.copy(document = null) } }

    fun viewerUnavailable() {
        _uiState.update { it.copy(errorMessage = "Файл скачан, но нет приложения для его открытия. Установите просмотрщик документов и нажмите «Резюме» ещё раз.") }
    }
}
