package dev.code93.daviplata.domain.usecase.auth

import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.AuthRepository
import dev.code93.daviplata.domain.repository.SessionRepository
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val sessionRepo: SessionRepository,
) {
    operator fun invoke(phone: String, password: String): Flow<ApiResult<Session>> {
        Validators.firstError(
            Validators.phone(phone),
            Validators.password(password),
        )?.let { return flowOf(ApiResult.Failure(AppError.Validation(it))) }

        return authRepo.login(phone, password)
            .onEach { if (it is ApiResult.Success) sessionRepo.save(it.data) }
    }
}
