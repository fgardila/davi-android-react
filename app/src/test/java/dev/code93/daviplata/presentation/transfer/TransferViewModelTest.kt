package dev.code93.daviplata.presentation.transfer

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.model.Balance
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.usecase.account.GetBalanceUseCase
import dev.code93.daviplata.domain.usecase.session.GetCurrentSessionUseCase
import dev.code93.daviplata.domain.usecase.transfer.CreateTransferUseCase
import dev.code93.daviplata.domain.usecase.transfer.FindRecipientUseCase
import dev.code93.daviplata.presentation.transfer.RecipientState
import dev.code93.daviplata.presentation.transfer.SubmitState
import dev.code93.daviplata.presentation.transfer.TransferViewModel
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
class TransferViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val getBalance: GetBalanceUseCase = mockk()
    private val findRecipient: FindRecipientUseCase = mockk()
    private val createTransfer: CreateTransferUseCase = mockk()
    private val getCurrentSession: GetCurrentSessionUseCase = mockk()

    private val currentSession = Session(
        sessionId = "sid", userId = "u-001", name = "Fabian",
        phone = "3001234567", expiresAtMillis = Long.MAX_VALUE,
    )

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getCurrentSession() } returns currentSession
        // El init del VM dispara loadBalance() → stub mínimo Success.
        every { getBalance() } returns flowOf(
            ApiResult.Success(Balance(amount = 1_250_345.0, currency = "COP")),
        )
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun newVm() = TransferViewModel(getBalance, findRecipient, createTransfer, getCurrentSession)

    // ─── loadBalance (init) ────────────────────────────────────────────────

    @Test fun `init loads balance and clears loading`() = runTest(dispatcher) {
        val vm = newVm()
        advanceUntilIdle()

        assertEquals(1_250_345.0, vm.state.value.balance, 0.001)
        assertEquals(false, vm.state.value.balanceLoading)
    }

    @Test fun `balance Failure clears loading without crashing`() = runTest(dispatcher) {
        every { getBalance() } returns flowOf(ApiResult.Failure(AppError.NetworkError))
        val vm = newVm()
        advanceUntilIdle()

        assertEquals(false, vm.state.value.balanceLoading)
    }

    // ─── lookupRecipient ───────────────────────────────────────────────────

    @Test fun `lookup with less than 10 digits stays Idle`() = runTest(dispatcher) {
        val vm = newVm()
        advanceUntilIdle()

        vm.lookupRecipient("300")
        advanceUntilIdle()

        assertEquals(RecipientState.Idle, vm.state.value.recipientState)
    }

    @Test fun `lookup with own phone emits SelfTransfer without calling repo`() = runTest(dispatcher) {
        val vm = newVm()
        advanceUntilIdle()

        vm.lookupRecipient("3001234567") // mismo número de la sesión
        advanceUntilIdle()

        assertEquals(RecipientState.SelfTransfer, vm.state.value.recipientState)
        io.mockk.verify(exactly = 0) { findRecipient(any()) }
    }

    @Test fun `lookup with valid foreign phone emits Found`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient("3009876543") } returns
            flowOf(ApiResult.Loading, ApiResult.Success(recipient))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()

        val state = vm.state.value.recipientState as RecipientState.Found
        assertEquals(recipient, state.recipient)
    }

    @Test fun `lookup with unknown phone (Success null) emits NotFound`() = runTest(dispatcher) {
        every { findRecipient("0000000000") } returns
            flowOf(ApiResult.Success(null))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("0000000000")
        advanceUntilIdle()

        assertEquals(RecipientState.NotFound, vm.state.value.recipientState)
    }

    @Test fun `lookup NetworkError emits Error state`() = runTest(dispatcher) {
        every { findRecipient(any()) } returns
            flowOf(ApiResult.Failure(AppError.NetworkError))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()

        val state = vm.state.value.recipientState as RecipientState.Error
        assertTrue(state.message.contains("conexión"))
    }

    // ─── submit ────────────────────────────────────────────────────────────

    @Test fun `submit without selected recipient is no-op`() = runTest(dispatcher) {
        val vm = newVm()
        advanceUntilIdle()

        vm.submit("3009876543", 50_000.0, "concepto")
        advanceUntilIdle()

        // submitState sigue Idle porque no había recipient seleccionado.
        assertEquals(SubmitState.Idle, vm.state.value.submitState)
        io.mockk.verify(exactly = 0) { createTransfer(any(), any(), any()) }
    }

    @Test fun `submit success updates balance and submitState`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient("3009876543") } returns flowOf(ApiResult.Success(recipient))
        val transfer = Transfer(
            transferId = "tx", toPhone = "3009876543", amount = 50_000.0,
            status = "COMPLETED", newBalance = 1_200_345.0,
            createdAtMillis = System.currentTimeMillis(),
        )
        every { createTransfer("3009876543", 50_000.0, "lunch") } returns
            flowOf(ApiResult.Loading, ApiResult.Success(transfer))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()

        vm.submit("3009876543", 50_000.0, "lunch")
        advanceUntilIdle()

        val success = vm.state.value.submitState as SubmitState.Success
        assertEquals(transfer, success.transfer)
        assertEquals("Ana Gómez", success.recipientName)
        assertEquals(1_200_345.0, vm.state.value.balance, 0.001)
    }

    @Test fun `submit InsufficientFunds emits specific code`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient(any()) } returns flowOf(ApiResult.Success(recipient))
        every { createTransfer(any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InsufficientFunds))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()
        vm.submit("3009876543", 99_999_999.0, "x")
        advanceUntilIdle()

        val error = vm.state.value.submitState as SubmitState.Error
        assertEquals("INSUFFICIENT_FUNDS", error.code)
    }

    @Test fun `submit RecipientNotFound emits specific code`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient(any()) } returns flowOf(ApiResult.Success(recipient))
        every { createTransfer(any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.RecipientNotFound))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()
        vm.submit("3009876543", 100.0, "x")
        advanceUntilIdle()

        val error = vm.state.value.submitState as SubmitState.Error
        assertEquals("RECIPIENT_NOT_FOUND", error.code)
    }

    @Test fun `submit Validation error uses validation message`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient(any()) } returns flowOf(ApiResult.Success(recipient))
        every { createTransfer(any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.Validation("Monto inválido")))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()
        vm.submit("3009876543", -1.0, "x")
        advanceUntilIdle()

        val error = vm.state.value.submitState as SubmitState.Error
        assertEquals("Monto inválido", error.message)
    }

    @Test fun `resetSubmitState returns submitState to Idle`() = runTest(dispatcher) {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { findRecipient(any()) } returns flowOf(ApiResult.Success(recipient))
        every { createTransfer(any(), any(), any()) } returns
            flowOf(ApiResult.Failure(AppError.InsufficientFunds))

        val vm = newVm()
        advanceUntilIdle()
        vm.lookupRecipient("3009876543")
        advanceUntilIdle()
        vm.submit("3009876543", 1_000_000.0, "x")
        advanceUntilIdle()
        assertTrue(vm.state.value.submitState is SubmitState.Error)

        vm.resetSubmitState()
        assertEquals(SubmitState.Idle, vm.state.value.submitState)
    }
}
