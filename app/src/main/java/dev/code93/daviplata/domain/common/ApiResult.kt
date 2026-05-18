package dev.code93.daviplata.domain.common

sealed interface ApiResult<out T> {
    data object Loading : ApiResult<Nothing>
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: AppError) : ApiResult<Nothing>
}
