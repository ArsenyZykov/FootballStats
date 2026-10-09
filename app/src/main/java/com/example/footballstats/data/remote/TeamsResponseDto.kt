package com.example.footballstats.data.remote

import com.google.gson.annotations.SerializedName

data class TeamsResponseDto(
    val status: String?,
    val count: Int?,
    val data: List<TeamDto>?,
    @SerializedName("TotalCount")
    val totalCount: Int?
)

data class TeamDto(
    val id: Int,
    val name: String?,
    val flashId: String?,
    val logoUrl: String?,
    val country: CountryDto?
)

data class CountryDto(
    val code: String?,
    val name: String?
)