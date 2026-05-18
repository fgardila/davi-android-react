package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovementItemDto(
    val id: String,
    val type: String,
    val status: String,
    val amount: Double,
    val description: String,
    val occurredAt: Long,
)
