package dev.code93.daviplata.data.remote.error

import com.squareup.moshi.Moshi
import dev.code93.daviplata.data.remote.error.ErrorMapper
import dev.code93.daviplata.data.remote.error.apiFlow
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.common.AppError
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ApiFlowTest {

    private val mapper = ErrorMapper(Moshi.Builder().build())

    @Test
    fun `success block emits Loading then Success`() = runTest {
        val emissions = apiFlow(mapper) { "payload" }.toList()

        assertEquals(2, emissions.size)
        assertEquals(ApiResult.Loading, emissions[0])
        assertEquals(ApiResult.Success("payload"), emissions[1])
    }

    @Test
    fun `IOException emits Loading then Failure(NetworkError)`() = runTest {
        val emissions = apiFlow(mapper) {
            throw IOException("offline")
        }.toList()

        assertEquals(2, emissions.size)
        assertEquals(ApiResult.Loading, emissions[0])
        val failure = emissions[1] as ApiResult.Failure
        assertEquals(AppError.NetworkError, failure.error)
    }

    @Test
    fun `IllegalArgumentException emits Failure(Validation)`() = runTest {
        val emissions = apiFlow(mapper) {
            require(false) { "phone required" }
            "never"
        }.toList()

        val failure = emissions.last() as ApiResult.Failure
        assertTrue(failure.error is AppError.Validation)
        assertEquals("phone required", (failure.error as AppError.Validation).msg)
    }

    @Test
    fun `arbitrary throwable emits Failure(Unknown)`() = runTest {
        val emissions = apiFlow(mapper) {
            throw IllegalStateException("boom")
            @Suppress("UNREACHABLE_CODE") "never"
        }.toList()

        val failure = emissions.last() as ApiResult.Failure
        assertTrue(failure.error is AppError.Unknown)
    }
}
