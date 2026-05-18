package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    val phone: String,
    val name: String,
    val document: String,
    val email: String,
    val username: String,
    val password: String,
)
