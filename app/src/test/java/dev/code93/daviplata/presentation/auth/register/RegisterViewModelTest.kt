package dev.code93.daviplata.presentation.auth.register

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.usecase.auth.RegisterUseCase
import dev.code93.daviplata.presentation.auth.register.PasswordStrength
import dev.code93.daviplata.presentation.auth.register.RegisterUiState
import dev.code93.daviplata.presentation.auth.register.RegisterViewModel
import dev.code93.daviplata.presentation.auth.register.evaluatePasswordStrength
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
class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val registerUseCase: RegisterUseCase = mockk()
    private lateinit var vm: RegisterViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = RegisterViewModel(registerUseCase)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // ─── evaluatePasswordStrength (top-level function) ─────────────────────

    @Test fun `strength Weak for very short password`() {
        assertEquals(PasswordStrength.Weak, evaluatePasswordStrength("abc"))
    }

    @Test fun `strength Weak for length-only password`() {
        // 8 chars, sin upper/digit/symbol → score 1 → Weak
        assertEquals(PasswordStrength.Weak, evaluatePasswordStrength("abcdefgh"))
    }

    @Test fun `strength Fair for length+upper`() {
        // 8 chars + upper → score 2 → Fair
        assertEquals(PasswordStrength.Fair, evaluatePasswordStrength("Abcdefgh"))
    }

    @Test fun `strength Good for length+upper+digit`() {
        assertEquals(PasswordStrength.Good, evaluatePasswordStrength("Abcdefg1"))
    }

    @Test fun `strength Strong for length 12+upper+digit`() {
        assertEquals(PasswordStrength.Strong, evaluatePasswordStrength("Abcdefghij12"))
    }

    @Test fun `strength Strong for length+upper+digit+symbol`() {
        assertEquals(PasswordStrength.Strong, evaluatePasswordStrength("Abcdef1!"))
    }

    // ─── Reducer ───────────────────────────────────────────────────────────

    @Test fun `initial state is Idle`() {
        assertEquals(RegisterUiState.Idle, vm.uiState.value)
    }

    @Test fun `successful registration emits Success`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Loading, ApiResult.Success(Unit))

        vm.register("3001234567", "Felipe", "1234567890",
            "f@demo.co", "felipe", "Demo1234", "Demo1234")
        advanceUntilIdle()

        assertEquals(RegisterUiState.Success, vm.uiState.value)
    }

    @Test fun `Loading state visible during emission`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Loading)

        vm.register("3001234567", "Felipe", "1234567890",
            "f@demo.co", "felipe", "Demo1234", "Demo1234")
        advanceUntilIdle()

        assertEquals(RegisterUiState.Loading, vm.uiState.value)
    }

    @Test fun `PhoneTaken maps to specific error message`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.PhoneTaken))

        vm.register("3001234567", "Felipe", "1234567890",
            "f@demo.co", "felipe", "Demo1234", "Demo1234")
        advanceUntilIdle()

        val state = vm.uiState.value as RegisterUiState.Error
        assertTrue(state.message.contains("ya está registrado"))
    }

    @Test fun `Validation error forwards its message`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.Validation("Documento inválido")))

        vm.register("x", "x", "x", "x", "x", "x", "x")
        advanceUntilIdle()

        assertEquals("Documento inválido",
            (vm.uiState.value as RegisterUiState.Error).message)
    }

    @Test fun `Unknown error maps to generic message`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.NetworkError))

        vm.register("3001234567", "Felipe", "1234567890",
            "f@demo.co", "felipe", "Demo1234", "Demo1234")
        advanceUntilIdle()

        val state = vm.uiState.value as RegisterUiState.Error
        assertTrue(state.message.contains("Algo salió mal"))
    }

    @Test fun `resetState returns to Idle`() = runTest(dispatcher) {
        every { registerUseCase(any(), any(), any(), any(), any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.PhoneTaken))
        vm.register("3001234567", "Felipe", "1234567890",
            "f@demo.co", "felipe", "Demo1234", "Demo1234")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is RegisterUiState.Error)

        vm.resetState()
        assertEquals(RegisterUiState.Idle, vm.uiState.value)
    }
}
