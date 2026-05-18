package dev.code93.daviplata.data.remote.api

import dev.code93.daviplata.data.remote.dto.*
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/auth/logout")
    suspend fun logout(): Unit

    @GET("api/users/{phone}")
    suspend fun findUser(@Path("phone") phone: String): UserDto

    @GET("api/accounts/balance")
    suspend fun getBalance(): BalanceResponseDto

    @GET("api/movements")
    suspend fun getMovements(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): MovementsResponseDto

    @POST("api/transfers")
    suspend fun createTransfer(@Body request: TransferRequestDto): TransferResponseDto

    @GET("api/session/validate")
    suspend fun validateSession(): SessionValidateResponseDto
}
