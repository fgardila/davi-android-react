package dev.code93.daviplata.presentation.auth.login

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.usecase.auth.LoginUseCase
import dev.code93.daviplata.presentation.auth.login.LoginUiState
import dev.code93.daviplata.presentation.auth.login.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var vm: LoginViewModel

    private val stubSession = Session(
        sessionId = "sid", userId = "u-001", name = "Fabian",
        phone = "3001234567", expiresAtMillis = Long.MAX_VALUE,
    )

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = LoginViewModel(loginUseCase)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `initial state is Idle`() {
        assertEquals(LoginUiState.Idle, vm.uiState.value)
    }

    @Test fun `Loading then Success transitions state`() = runTest(dispatcher) {
        every { loginUseCase("3001234567", "demo1234") } returns
            flowOf(ApiResult.Loading, ApiResult.Success(stubSession))

        vm.login("3001234567", "demo1234")
        advanceUntilIdle()

        assertEquals(LoginUiState.Success(stubSession), vm.uiState.value)
    }

    @Test fun `InvalidCredentials maps to Error with attempts`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InvalidCredentials(attempts = 2)))

        vm.login("3001234567", "wrong1234")
        advanceUntilIdle()

        val state = vm.uiState.value as LoginUiState.Error
        assertEquals(2, state.attemptCount)
        assertTrue(state.message.contains("incorrectos"))
    }

    @Test fun `InvalidCredentials with null attempts defaults to 1`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InvalidCredentials(attempts = null)))

        vm.login("3001234567", "wrong1234")
        advanceUntilIdle()

        assertEquals(1, (vm.uiState.value as LoginUiState.Error).attemptCount)
    }

    @Test fun `AccountLocked maps to Locked with retryAfterSeconds`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.AccountLocked(retryAfterSeconds = 120)))

        vm.login("3001234567", "wrong1234")
        advanceUntilIdle()

        assertEquals(LoginUiState.Locked(120), vm.uiState.value)
    }

    @Test fun `Validation error maps to Error with that message`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.Validation("Phone required")))

        vm.login("123", "demo1234")
        advanceUntilIdle()

        assertEquals("Phone required", (vm.uiState.value as LoginUiState.Error).message)
    }

    @Test fun `Unknown error maps to generic Error`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.NetworkError))

        vm.login("3001234567", "demo1234")
        advanceUntilIdle()

        val state = vm.uiState.value as LoginUiState.Error
        assertTrue(state.message.contains("Algo salió mal"))
    }

    @Test fun `concurrent login while Loading is ignored`() = runTest(dispatcher) {
        // Primer call: dispatcher pendiente → estado se queda en Loading
        every { loginUseCase("3001234567", "demo1234") } returns
            flowOf(ApiResult.Loading)
        vm.login("3001234567", "demo1234")
        advanceUntilIdle()
        assertEquals(LoginUiState.Loading, vm.uiState.value)

        // Segundo call mientras Loading: no debe llamar al UseCase de nuevo.
        // Lo verificamos cambiando el stub: si lo invocara, daría Success.
        every { loginUseCase("3001234567", "demo1234") } returns
            flowOf(ApiResult.Success(stubSession))
        vm.login("3001234567", "demo1234")
        advanceUntilIdle()

        // Sigue en Loading porque el guard del VM bloqueó el segundo invoke.
        assertEquals(LoginUiState.Loading, vm.uiState.value)
    }

    @Test fun `resetState returns to Idle`() = runTest(dispatcher) {
        every { loginUseCase(any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InvalidCredentials(1)))
        vm.login("3001234567", "wrong1234")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is LoginUiState.Error)

        vm.resetState()
        assertEquals(LoginUiState.Idle, vm.uiState.value)
    }
}
