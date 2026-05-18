package dev.code93.daviplata.domain.usecase.transfer

import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.repository.TransferRepository
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FindRecipientUseCase @Inject constructor(
    private val transferRepo: TransferRepository,
) {
    operator fun invoke(phone: String): Flow<ApiResult<Recipient?>> {
        Validators.phone(phone)?.let {
            return flowOf(ApiResult.Failure(AppError.Validation(it)))
        }
        return transferRepo.findRecipient(phone)
    }
}
