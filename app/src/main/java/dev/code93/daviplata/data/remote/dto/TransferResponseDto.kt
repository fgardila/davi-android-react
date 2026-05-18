package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransferResponseDto(
    val transferId: String,
    val status: String,
    val newBalance: Double,
    val createdAt: Long,
    val recipientName: String,
)
