package com.example.footballstats.domain.model

data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String = "",
    val resumeUrl: String = ""
)

data class ProfileDocument(val uri: String, val mimeType: String)
