package dev.code93.daviplata.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponseDto(val userId: String, val name: String, val phone: String)
