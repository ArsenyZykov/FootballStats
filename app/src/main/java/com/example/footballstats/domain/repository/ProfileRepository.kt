package com.example.footballstats.domain.repository

import com.example.footballstats.domain.model.ProfileDocument
import com.example.footballstats.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun save(profile: UserProfile)
    suspend fun importAvatar(sourceUri: String): String
    suspend fun createCameraPhoto(): String
    suspend fun discardPhoto(uri: String)
    suspend fun downloadResume(url: String): ProfileDocument
}
