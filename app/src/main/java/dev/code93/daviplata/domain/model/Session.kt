package dev.code93.daviplata.domain.model

data class Session(
    val sessionId: String,
    val userId: String,
    val name: String,
    val phone: String,
    val expiresAtMillis: Long,
)
