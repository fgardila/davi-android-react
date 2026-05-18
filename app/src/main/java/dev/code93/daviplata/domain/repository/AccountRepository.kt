package dev.code93.daviplata.domain.repository

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Balance
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getBalance(): Flow<ApiResult<Balance>>
}
