package dev.code93.daviplata.domain.model

data class Balance(
    val amount: Double,
    val currency: String = "COP",
)
