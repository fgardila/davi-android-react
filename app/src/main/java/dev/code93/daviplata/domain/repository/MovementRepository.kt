package dev.code93.daviplata.domain.repository

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.MovementsPage
import kotlinx.coroutines.flow.Flow

interface MovementRepository {
    fun getMovements(page: Int, size: Int): Flow<ApiResult<MovementsPage>>
}
