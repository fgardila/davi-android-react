package dev.code93.daviplata.domain.usecase.account

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Balance
import dev.code93.daviplata.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBalanceUseCase @Inject constructor(
    private val accountRepo: AccountRepository,
) {
    operator fun invoke(): Flow<ApiResult<Balance>> = accountRepo.getBalance()
}
