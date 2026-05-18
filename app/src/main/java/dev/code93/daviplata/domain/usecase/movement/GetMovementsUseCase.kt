package dev.code93.daviplata.domain.usecase.movement

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.MovementsPage
import dev.code93.daviplata.domain.repository.MovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovementsUseCase @Inject constructor(
    private val movementRepo: MovementRepository,
) {
    operator fun invoke(page: Int, size: Int = 20): Flow<ApiResult<MovementsPage>> =
        movementRepo.getMovements(page, size)
}
