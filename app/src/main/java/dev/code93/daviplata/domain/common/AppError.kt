package dev.code93.daviplata.domain.common

sealed class AppError(val message: String? = null) {
    data class InvalidCredentials(val attempts: Int?) : AppError()
    data class AccountLocked(val retryAfterSeconds: Int) : AppError()
    data object SessionExpired : AppError()
    data object PhoneTaken : AppError()
    data object InsufficientFunds : AppError()
    data object RecipientNotFound : AppError()
    data object InvalidAmount : AppError()
    data object UserNotFound : AppError()
    data object ServerError : AppError()
    data object NetworkError : AppError()
    data class Validation(val msg: String) : AppError(msg)
    data class Unknown(val msg: String?) : AppError(msg)
}
