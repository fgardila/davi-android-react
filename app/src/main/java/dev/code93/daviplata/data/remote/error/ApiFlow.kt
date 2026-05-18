package dev.code93.daviplata.data.remote.error

import dev.code93.daviplata.domain.common.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal fun <T> apiFlow(mapper: ErrorMapper, block: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)
    emit(ApiResult.Success(block()))
}.catch { e -> emit(ApiResult.Failure(mapper.map(e))) }
