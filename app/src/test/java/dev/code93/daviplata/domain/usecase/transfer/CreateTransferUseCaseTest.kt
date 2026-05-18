package dev.code93.daviplata.domain.usecase.transfer

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.repository.TransferRepository
import dev.code93.daviplata.domain.usecase.transfer.CreateTransferUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTransferUseCaseTest {

    private val transferRepo: TransferRepository = mockk()
    private val useCase = CreateTransferUseCase(transferRepo)

    private val stubTransfer = Transfer(
        transferId = "tx1",
        toPhone = "3009876543",
        amount = 50_000.0,
        status = "COMPLETED",
        newBalance = 1_200_345.0,
        createdAtMillis = System.currentTimeMillis(),
    )

    @Test
    fun `valid transfer invokes repo and returns Success`() = runTest {
        every { transferRepo.create(any(), any(), any()) } returns flowOf(ApiResult.Success(stubTransfer))
        val result = useCase("3009876543", 50_000.0, "Test").first { it !is ApiResult.Loading }
        assertEquals(ApiResult.Success(stubTransfer), result)
    }

    @Test
    fun `phone shorter than 10 digits emits Validation failure`() = runTest {
        val result = useCase("300123456", 50_000.0, "Test").first()
        assertTrue(result is ApiResult.Failure && result.error is AppError.Validation)
    }

    @Test
    fun `phone longer than 10 digits emits Validation failure`() = runTest {
        val result = useCase("30012345678", 50_000.0, "Test").first()
        assertTrue(result is ApiResult.Failure && result.error is AppError.Validation)
    }

    @Test
    fun `zero amount emits Validation failure`() = runTest {
        val result = useCase("3009876543", 0.0, "Test").first()
        assertTrue(result is ApiResult.Failure && result.error is AppError.Validation)
    }

    @Test
    fun `negative amount emits Validation failure`() = runTest {
        val result = useCase("3009876543", -100.0, "Test").first()
        assertTrue(result is ApiResult.Failure && result.error is AppError.Validation)
    }
}
