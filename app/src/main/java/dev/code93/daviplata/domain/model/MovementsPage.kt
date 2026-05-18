package dev.code93.daviplata.domain.model

data class MovementsPage(
    val items: List<Movement>,
    val total: Int,
    val page: Int,
)
