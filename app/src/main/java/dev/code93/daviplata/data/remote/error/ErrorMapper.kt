package dev.code93.daviplata.data.remote.error

import com.squareup.moshi.Moshi
import dev.code93.daviplata.data.remote.dto.ErrorDto
import dev.code93.daviplata.domain.common.AppError
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMapper @Inject constructor(private val moshi: Moshi) {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is HttpException -> mapHttp(throwable)
        is IOException -> AppError.NetworkError
        is IllegalArgumentException -> AppError.Validation(throwable.message ?: "Datos inválidos")
        else -> AppError.Unknown(throwable.message)
    }

    private fun mapHttp(e: HttpException): AppError {
        val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val dto = rawBody?.let {
            runCatching { moshi.adapter(ErrorDto::class.java).fromJson(it) }.getOrNull()
        }
        val code = dto?.code
        return when (e.code() to code) {
            400 to "INVALID_AMOUNT" -> AppError.InvalidAmount
            401 to "INVALID_CREDENTIALS" -> AppError.InvalidCredentials(extractInt(rawBody, "attempts"))
            401 to "SESSION_EXPIRED" -> AppError.SessionExpired
            404 to "USER_NOT_FOUND" -> AppError.UserNotFound
            404 to "RECIPIENT_NOT_FOUND" -> AppError.RecipientNotFound
            404 to "NOT_FOUND" -> AppError.Unknown(dto?.message)
            409 to "PHONE_TAKEN" -> AppError.PhoneTaken
            409 to "INSUFFICIENT_FUNDS" -> AppError.InsufficientFunds
            423 to "ACCOUNT_LOCKED" -> AppError.AccountLocked(extractInt(rawBody, "retryAfterSeconds") ?: 300)
            500 to "INTERNAL_ERROR" -> AppError.ServerError
            else -> AppError.Unknown(dto?.message ?: e.message())
        }
    }

    private fun extractInt(body: String?, key: String): Int? =
        body?.let { Regex(""""$key"\s*:\s*(\d+)""").find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }
}
