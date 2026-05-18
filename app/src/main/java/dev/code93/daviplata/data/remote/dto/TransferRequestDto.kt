package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransferRequestDto(
    val toPhone: String,
    val amount: Double,
    val description: String,
)
