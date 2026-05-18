package dev.code93.daviplata.domain.model

data class User(
    val userId: String,
    val name: String,
    val phone: String,
    val email: String,
    val document: String,
)
