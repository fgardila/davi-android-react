package dev.code93.daviplata.domain.model

data class Transfer(
    val transferId: String,
    val toPhone: String,
    val amount: Double,
    val status: String,
    val newBalance: Double,
    val createdAtMillis: Long,
)
