package dev.code93.daviplata.data.repository

import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.dto.LoginRequestDto
import dev.code93.daviplata.data.remote.dto.RegisterRequestDto
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.remote.error.apiFlow
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val errorMapper: ErrorMapper,
) : AuthRepository {
    override fun login(phone: String, password: String): Flow<ApiResult<Session>> = apiFlow(errorMapper) {
        val dto = api.login(LoginRequestDto(phone, password))
        Session(
            sessionId = dto.sessionId,
            userId = dto.userId,
            name = dto.name,
            phone = dto.phone,
            // Mock server returns absolute expiresAt in millis; respect it directly
            expiresAtMillis = dto.expiresAt,
        )
    }

    override fun register(phone: String, name: String, document: String, email: String, username: String, password: String): Flow<ApiResult<Unit>> =
        apiFlow(errorMapper) {
            api.register(RegisterRequestDto(phone, name, document, email, username, password))
            Unit
        }

    override suspend fun logout() {
        runCatching { api.logout() }
    }
}
