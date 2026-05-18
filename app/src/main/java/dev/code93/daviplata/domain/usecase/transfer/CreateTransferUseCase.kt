package dev.code93.daviplata.domain.usecase.transfer

import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.repository.TransferRepository
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CreateTransferUseCase @Inject constructor(
    private val transferRepo: TransferRepository,
) {
    operator fun invoke(toPhone: String, amount: Double, description: String): Flow<ApiResult<Transfer>> {
        Validators.firstError(
            Validators.phone(toPhone),
            Validators.amount(amount),
        )?.let { return flowOf(ApiResult.Failure(AppError.Validation(it))) }

        return transferRepo.create(toPhone, amount, description)
    }
}
