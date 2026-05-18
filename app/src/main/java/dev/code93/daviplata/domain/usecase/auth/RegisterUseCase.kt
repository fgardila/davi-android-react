package dev.code93.daviplata.domain.usecase.auth

import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.repository.AuthRepository
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepo: AuthRepository,
) {
    operator fun invoke(
        phone: String,
        name: String,
        document: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
    ): Flow<ApiResult<Unit>> {
        Validators.firstError(
            Validators.phone(phone),
            Validators.name(name),
            Validators.document(document),
            Validators.email(email),
            Validators.username(username),
            Validators.strongPassword(password),
            Validators.passwordConfirmation(password, confirmPassword),
        )?.let { return flowOf(ApiResult.Failure(AppError.Validation(it))) }

        return authRepo.register(phone, name, document, email, username, password)
    }
}
