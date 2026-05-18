package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.remote.error.apiFlow
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Balance
import dev.code93.daviplata.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val errorMapper: ErrorMapper,
) : AccountRepository {
    override fun getBalance(): Flow<ApiResult<Balance>> = apiFlow(errorMapper) {
        val dto = api.getBalance()
        Balance(amount = dto.balance, currency = dto.currency)
    }
}
