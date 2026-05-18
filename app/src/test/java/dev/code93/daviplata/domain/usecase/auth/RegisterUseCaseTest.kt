package dev.code93.daviplata.domain.usecase.auth

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.repository.AuthRepository
import dev.code93.daviplata.domain.usecase.auth.RegisterUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val authRepo: AuthRepository = mockk()
    private val useCase = RegisterUseCase(authRepo)

    private val validPayload = Args(
        phone = "3001234567",
        name = "Fabian Ardila",
        document = "1234567890",
        email = "fabian@demo.co",
        username = "fabian.ardila",
        password = "Demo1234",
        confirmPassword = "Demo1234",
    )

    @Test
    fun `valid input forwards to repo and emits Success`() = runTest {
        every { authRepo.register(any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Loading, ApiResult.Success(Unit))

        val results = with(validPayload) {
            useCase(phone, name, document, email, username, password, confirmPassword).toList()
        }

        assertEquals(ApiResult.Loading, results[0])
        assertEquals(ApiResult.Success(Unit), results[1])
        coVerify(exactly = 1) {
            authRepo.register("3001234567", "Fabian Ardila", "1234567890",
                "fabian@demo.co", "fabian.ardila", "Demo1234")
        }
    }

    @Test fun `invalid phone short-circuits`() = runTest {
        val result = invoke(validPayload.copy(phone = "300")).first()
        assertValidation(result, "10 dígitos")
    }

    @Test fun `blank name short-circuits`() = runTest {
        val result = invoke(validPayload.copy(name = "")).first()
        assertValidation(result, "nombre")
    }

    @Test fun `short document short-circuits`() = runTest {
        val result = invoke(validPayload.copy(document = "123")).first()
        assertValidation(result, "documento")
    }

    @Test fun `email without at-sign short-circuits`() = runTest {
        val result = invoke(validPayload.copy(email = "not-an-email")).first()
        assertValidation(result, "correo")
    }

    @Test fun `username with space short-circuits`() = runTest {
        val result = invoke(validPayload.copy(username = "fabian ardila")).first()
        assertValidation(result, "usuario")
    }

    @Test fun `password missing uppercase short-circuits`() = runTest {
        val result = invoke(validPayload.copy(password = "demo1234", confirmPassword = "demo1234")).first()
        assertValidation(result, "mayúscula")
    }

    @Test fun `password missing digit short-circuits`() = runTest {
        val result = invoke(validPayload.copy(password = "Password", confirmPassword = "Password")).first()
        assertValidation(result, "número")
    }

    @Test fun `password mismatch short-circuits`() = runTest {
        val result = invoke(validPayload.copy(confirmPassword = "Otro1234")).first()
        assertValidation(result, "coinciden")
    }

    @Test fun `repo Failure propagates without retry`() = runTest {
        every { authRepo.register(any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.PhoneTaken))

        val result = invoke(validPayload).first { it !is ApiResult.Loading }

        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.PhoneTaken, (result as ApiResult.Failure).error)
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun invoke(a: Args) = with(a) {
        useCase(phone, name, document, email, username, password, confirmPassword)
    }

    private fun assertValidation(result: ApiResult<*>, msgContains: String) {
        assertTrue("expected Failure but got $result", result is ApiResult.Failure)
        val error = (result as ApiResult.Failure).error
        assertTrue("expected Validation but got $error", error is AppError.Validation)
        val msg = (error as AppError.Validation).msg.lowercase()
        assertTrue("'${error.msg}' should mention '$msgContains'",
            msg.contains(msgContains.lowercase()))
    }

    private data class Args(
        val phone: String, val name: String, val document: String, val email: String,
        val username: String, val password: String, val confirmPassword: String,
    )
}
