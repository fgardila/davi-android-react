package dev.code93.daviplata.domain.model

enum class MovementType { DEBIT, CREDIT }

enum class MovementStatus { COMPLETED, PENDING, FAILED }

data class Movement(
    val id: String,
    val type: MovementType,
    val status: MovementStatus,
    val amount: Double,
    val description: String,
    val occurredAtMillis: Long,
)
