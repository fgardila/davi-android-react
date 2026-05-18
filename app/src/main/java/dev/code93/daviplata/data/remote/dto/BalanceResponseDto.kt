package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BalanceResponseDto(val userId: String, val balance: Double, val currency: String)
