package dev.code93.daviplata.domain.repository

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.model.Transfer
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun create(toPhone: String, amount: Double, description: String): Flow<ApiResult<Transfer>>
    fun findRecipient(phone: String): Flow<ApiResult<Recipient?>>
}
