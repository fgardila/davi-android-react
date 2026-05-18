package dev.code93.daviplata.data.repository

import com.squareup.moshi.Moshi
import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.dto.TransferResponseDto
import dev.code93.daviplata.data.remote.dto.UserDto
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.repository.TransferRepositoryImpl
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TransferRepositoryImplTest {

    private val api: ApiService = mockk()
    private val mapper = ErrorMapper(Moshi.Builder().build())
    private val repo = TransferRepositoryImpl(api, mapper)

    private fun httpError(code: Int, errorCode: String): HttpException {
        val body = """{"code":"$errorCode","message":"x"}""".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }

    // ─── create() ──────────────────────────────────────────────────────────

    @Test
    fun `create maps DTO to domain Transfer`() = runTest {
        coEvery { api.createTransfer(any()) } returns TransferResponseDto(
            transferId = "tx-1",
            status = "COMPLETED",
            newBalance = 1_000_000.0,
            createdAt = 123_456_789L,
            recipientName = "Ana Gómez",
        )

        val results = repo.create("3009876543", 50_000.0, "lunch").toList()
        val success = results.last() as ApiResult.Success

        assertEquals("tx-1", success.data.transferId)
        assertEquals("COMPLETED", success.data.status)
        assertEquals("3009876543", success.data.toPhone)
        assertEquals(50_000.0, success.data.amount, 0.001)
        assertEquals(1_000_000.0, success.data.newBalance, 0.001)
    }

    @Test
    fun `create propagates InsufficientFunds error`() = runTest {
        coEvery { api.createTransfer(any()) } throws httpError(409, "INSUFFICIENT_FUNDS")

        val result = repo.create("3009876543", 9_999_999.0, "x").toList().last()

        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.InsufficientFunds, (result as ApiResult.Failure).error)
    }

    // ─── findRecipient() — 404 special case ────────────────────────────────

    @Test
    fun `findRecipient returns Success with Recipient`() = runTest {
        coEvery { api.findUser("3009876543") } returns UserDto(
            userId = "u-002",
            name = "Ana Gómez",
            phone = "3009876543",
        )

        val result = repo.findRecipient("3009876543").toList().last() as ApiResult.Success
        assertEquals("Ana Gómez", result.data?.name)
        assertEquals("3009876543", result.data?.phone)
    }

    @Test
    fun `findRecipient maps RECIPIENT_NOT_FOUND to Success(null)`() = runTest {
        // El repo absorbe el 404 para no obligar al ViewModel a distinguir
        // "no encontrado" vs "error técnico" en la rama Failure.
        coEvery { api.findUser(any()) } throws httpError(404, "RECIPIENT_NOT_FOUND")

        val result = repo.findRecipient("0000000000").toList().last()

        assertTrue(result is ApiResult.Success)
        assertNull((result as ApiResult.Success).data)
    }

    @Test
    fun `findRecipient does NOT swallow other errors`() = runTest {
        coEvery { api.findUser(any()) } throws httpError(500, "INTERNAL_ERROR")

        val result = repo.findRecipient("3009876543").toList().last()

        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.ServerError, (result as ApiResult.Failure).error)
    }
}
