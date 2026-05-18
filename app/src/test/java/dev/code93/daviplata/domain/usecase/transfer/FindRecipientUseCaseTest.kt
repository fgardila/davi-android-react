package dev.code93.daviplata.domain.usecase.transfer

import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.repository.TransferRepository
import dev.code93.daviplata.domain.usecase.transfer.FindRecipientUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FindRecipientUseCaseTest {

    private val transferRepo: TransferRepository = mockk()
    private val useCase = FindRecipientUseCase(transferRepo)

    @Test
    fun `valid phone forwards to repo and emits Success`() = runTest {
        val recipient = Recipient(phone = "3009876543", name = "Ana Gómez")
        every { transferRepo.findRecipient("3009876543") } returns
            flowOf(ApiResult.Success(recipient))

        val result = useCase("3009876543").toList().last()

        assertEquals(ApiResult.Success(recipient), result)
        coVerify(exactly = 1) { transferRepo.findRecipient("3009876543") }
    }

    @Test
    fun `unknown phone propagates Success of null (404 mapping kept by repo)`() = runTest {
        every { transferRepo.findRecipient(any()) } returns
            flowOf(ApiResult.Success<Recipient?>(null))

        val result = useCase("0000000000").toList().last() as ApiResult.Success
        assertNull(result.data)
    }

    @Test
    fun `phone shorter than 10 short-circuits without hitting repo`() = runTest {
        val result = useCase("123").first()

        assertTrue(result is ApiResult.Failure)
        assertTrue((result as ApiResult.Failure).error is AppError.Validation)
        coVerify(exactly = 0) { transferRepo.findRecipient(any()) }
    }
}
