package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionValidateResponseDto(
    val valid: Boolean,
    val expiresAt: Long? = null,
    val remainingSeconds: Long? = null,
    val reason: String? = null,
)
