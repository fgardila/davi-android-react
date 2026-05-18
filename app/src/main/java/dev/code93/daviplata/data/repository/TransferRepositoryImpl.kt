package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.dto.TransferRequestDto
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.remote.error.apiFlow
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val errorMapper: ErrorMapper,
) : TransferRepository {
    override fun create(toPhone: String, amount: Double, description: String): Flow<ApiResult<Transfer>> =
        apiFlow(errorMapper) {
            val dto = api.createTransfer(TransferRequestDto(toPhone, amount, description))
            Transfer(
                transferId = dto.transferId,
                toPhone = toPhone,
                amount = amount,
                status = dto.status,
                newBalance = dto.newBalance,
                createdAtMillis = dto.createdAt,
            )
        }

    override fun findRecipient(phone: String): Flow<ApiResult<Recipient?>> =
        apiFlow(errorMapper) { Recipient(phone = phone, name = api.findUser(phone).name) }
            .map {
                if (it is ApiResult.Failure && it.error is AppError.RecipientNotFound)
                    ApiResult.Success(null)
                else it
            }
}
