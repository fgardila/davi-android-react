package dev.code93.daviplata.domain.repository

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(phone: String, password: String): Flow<ApiResult<Session>>
    fun register(phone: String, name: String, document: String, email: String, username: String, password: String): Flow<ApiResult<Unit>>
    suspend fun logout()
}
