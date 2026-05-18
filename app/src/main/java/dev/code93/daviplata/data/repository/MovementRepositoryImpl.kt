package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.remote.error.apiFlow
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Movement
import dev.code93.daviplata.domain.model.MovementStatus
import dev.code93.daviplata.domain.model.MovementType
import dev.code93.daviplata.domain.model.MovementsPage
import dev.code93.daviplata.domain.repository.MovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovementRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val errorMapper: ErrorMapper,
) : MovementRepository {
    override fun getMovements(page: Int, size: Int): Flow<ApiResult<MovementsPage>> = apiFlow(errorMapper) {
        val dto = api.getMovements(page, size)
        val items = dto.items.map { item ->
            Movement(
                id = item.id,
                type = if (item.type == "CREDIT") MovementType.CREDIT else MovementType.DEBIT,
                status = runCatching { MovementStatus.valueOf(item.status) }.getOrDefault(MovementStatus.COMPLETED),
                amount = item.amount,
                description = item.description,
                occurredAtMillis = item.occurredAt,
            )
        }
        MovementsPage(items = items, total = dto.total, page = page)
    }
}
