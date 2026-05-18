package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovementsResponseDto(
    val page: Int,
    val size: Int,
    val total: Int,
    val items: List<MovementItemDto>,
)
