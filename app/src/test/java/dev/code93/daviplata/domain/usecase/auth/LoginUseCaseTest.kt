package dev.code93.daviplata.domain.usecase.auth

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.repository.AuthRepository
import dev.code93.daviplata.domain.repository.SessionRepository
import dev.code93.daviplata.domain.usecase.auth.LoginUseCase
import io.mockk.coVerify
import io.mockk.coVerify as coVerifyAlias
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val authRepo: AuthRepository = mockk()
    private val sessionRepo: SessionRepository = mockk(relaxed = true)
    private val useCase = LoginUseCase(authRepo, sessionRepo)

    private val stubSession = Session(
        sessionId = "sid-xyz",
        userId = "u-001",
        name = "Fabian",
        phone = "3001234567",
        expiresAtMillis = System.currentTimeMillis() + 600_000L,
    )

    @Test
    fun `valid credentials emit Success and persist session`() = runTest {
        every { authRepo.login("3001234567", "demo1234") } returns
            flowOf(ApiResult.Loading, ApiResult.Success(stubSession))

        val results = useCase("3001234567", "demo1234").toList()

        assertEquals(ApiResult.Loading, results[0])
        assertEquals(ApiResult.Success(stubSession), results[1])
        coVerify(exactly = 1) { sessionRepo.save(stubSession) }
    }

    @Test
    fun `Failure from repo does NOT save session`() = runTest {
        every { authRepo.login(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InvalidCredentials(2)))

        useCase("3001234567", "demo1234").toList()

        coVerify(exactly = 0) { sessionRepo.save(any()) }
    }

    @Test
    fun `phone shorter than 10 short-circuits with Validation failure`() = runTest {
        val result = useCase("300123", "demo1234").first()

        assertTrue(result is ApiResult.Failure)
        assertTrue((result as ApiResult.Failure).error is AppError.Validation)
        coVerify(exactly = 0) { authRepo.login(any(), any()) }
        coVerify(exactly = 0) { sessionRepo.save(any()) }
    }

    @Test
    fun `password shorter than 8 short-circuits with Validation failure`() = runTest {
        val result = useCase("3001234567", "short").first()

        val failure = result as ApiResult.Failure
        assertTrue(failure.error is AppError.Validation)
        coVerify(exactly = 0) { authRepo.login(any(), any()) }
    }

    @Test
    fun `phone validation message comes from Validators`() = runTest {
        val result = useCase("123", "demo1234").first() as ApiResult.Failure
        val msg = (result.error as AppError.Validation).msg
        assertTrue(msg.contains("10 dígitos"))
    }
}
