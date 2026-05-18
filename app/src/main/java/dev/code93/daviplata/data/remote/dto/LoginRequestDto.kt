package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(val phone: String, val password: String)
