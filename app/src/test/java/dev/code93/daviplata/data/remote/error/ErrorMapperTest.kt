package dev.code93.daviplata.data.remote.error

import com.squareup.moshi.Moshi
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.domain.common.AppError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMapperTest {

    private val moshi: Moshi = Moshi.Builder().build()
    private val mapper = ErrorMapper(moshi)

    private fun httpError(code: Int, body: String): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, responseBody))
    }

    // ─── Throwable types (non-HTTP) ────────────────────────────────────────

    @Test fun `IOException maps to NetworkError`() {
        assertEquals(AppError.NetworkError, mapper.map(IOException("Connection refused")))
    }

    @Test fun `IllegalArgumentException maps to Validation`() {
        val result = mapper.map(IllegalArgumentException("Phone is required"))
        assertTrue(result is AppError.Validation)
        assertEquals("Phone is required", (result as AppError.Validation).msg)
    }

    @Test fun `unknown Throwable maps to Unknown`() {
        val result = mapper.map(RuntimeException("boom"))
        assertTrue(result is AppError.Unknown)
        assertEquals("boom", (result as AppError.Unknown).msg)
    }

    // ─── HTTP mapping table ────────────────────────────────────────────────

    @Test fun `400 INVALID_AMOUNT maps to InvalidAmount`() {
        val e = httpError(400, """{"code":"INVALID_AMOUNT","message":"Monto inválido"}""")
        assertEquals(AppError.InvalidAmount, mapper.map(e))
    }

    @Test fun `401 INVALID_CREDENTIALS extracts attempts`() {
        val e = httpError(401, """{"code":"INVALID_CREDENTIALS","message":"x","attempts":2}""")
        val result = mapper.map(e)
        assertTrue(result is AppError.InvalidCredentials)
        assertEquals(2, (result as AppError.InvalidCredentials).attempts)
    }

    @Test fun `401 INVALID_CREDENTIALS without attempts gives null`() {
        val e = httpError(401, """{"code":"INVALID_CREDENTIALS","message":"x"}""")
        val result = mapper.map(e) as AppError.InvalidCredentials
        assertEquals(null, result.attempts)
    }

    @Test fun `401 SESSION_EXPIRED maps to SessionExpired`() {
        val e = httpError(401, """{"code":"SESSION_EXPIRED","message":"expirada"}""")
        assertEquals(AppError.SessionExpired, mapper.map(e))
    }

    @Test fun `404 USER_NOT_FOUND maps to UserNotFound`() {
        val e = httpError(404, """{"code":"USER_NOT_FOUND","message":"x"}""")
        assertEquals(AppError.UserNotFound, mapper.map(e))
    }

    @Test fun `404 RECIPIENT_NOT_FOUND maps to RecipientNotFound`() {
        val e = httpError(404, """{"code":"RECIPIENT_NOT_FOUND","message":"x"}""")
        assertEquals(AppError.RecipientNotFound, mapper.map(e))
    }

    @Test fun `404 NOT_FOUND maps to Unknown with message`() {
        val e = httpError(404, """{"code":"NOT_FOUND","message":"Ruta inexistente"}""")
        val result = mapper.map(e)
        assertTrue(result is AppError.Unknown)
        assertEquals("Ruta inexistente", (result as AppError.Unknown).msg)
    }

    @Test fun `409 PHONE_TAKEN maps to PhoneTaken`() {
        val e = httpError(409, """{"code":"PHONE_TAKEN","message":"x"}""")
        assertEquals(AppError.PhoneTaken, mapper.map(e))
    }

    @Test fun `409 INSUFFICIENT_FUNDS maps to InsufficientFunds`() {
        val e = httpError(409, """{"code":"INSUFFICIENT_FUNDS","message":"x"}""")
        assertEquals(AppError.InsufficientFunds, mapper.map(e))
    }

    @Test fun `423 ACCOUNT_LOCKED extracts retryAfterSeconds`() {
        val e = httpError(423, """{"code":"ACCOUNT_LOCKED","message":"x","retryAfterSeconds":275}""")
        val result = mapper.map(e)
        assertTrue(result is AppError.AccountLocked)
        assertEquals(275, (result as AppError.AccountLocked).retryAfterSeconds)
    }

    @Test fun `423 ACCOUNT_LOCKED without retryAfter falls back to 300`() {
        val e = httpError(423, """{"code":"ACCOUNT_LOCKED","message":"x"}""")
        val result = mapper.map(e) as AppError.AccountLocked
        assertEquals(300, result.retryAfterSeconds)
    }

    @Test fun `500 INTERNAL_ERROR maps to ServerError`() {
        val e = httpError(500, """{"code":"INTERNAL_ERROR","message":"x"}""")
        assertEquals(AppError.ServerError, mapper.map(e))
    }

    // ─── Edge cases ────────────────────────────────────────────────────────

    @Test fun `unknown HTTP code with body falls back to Unknown`() {
        val e = httpError(418, """{"code":"IM_A_TEAPOT","message":"corto y robusto"}""")
        val result = mapper.map(e)
        assertTrue(result is AppError.Unknown)
        assertEquals("corto y robusto", (result as AppError.Unknown).msg)
    }

    @Test fun `corrupt JSON body falls back to Unknown`() {
        val e = httpError(500, "<<not json>>")
        val result = mapper.map(e)
        assertTrue(result is AppError.Unknown)
    }

    @Test fun `empty body falls back to Unknown`() {
        val e = httpError(500, "")
        val result = mapper.map(e)
        assertTrue(result is AppError.Unknown)
    }
}
