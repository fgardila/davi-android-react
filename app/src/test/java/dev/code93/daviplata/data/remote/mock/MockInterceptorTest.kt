package dev.code93.daviplata.data.remote.mock

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.code93.daviplata.data.remote.api.ApiService
import dev.code93.daviplata.data.remote.dto.LoginRequestDto
import dev.code93.daviplata.data.remote.dto.TransferRequestDto
import dev.code93.daviplata.data.remote.mock.MockDataStore
import dev.code93.daviplata.data.remote.mock.MockInterceptor
import dev.code93.daviplata.security.PasswordHasher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MockInterceptorTest {

    // Fresh store per test for isolation
    private lateinit var hasher: PasswordHasher
    private lateinit var store: MockDataStore
    private lateinit var moshi: Moshi
    private lateinit var api: ApiService

    @Before
    fun setup() {
        hasher = PasswordHasher()
        store = MockDataStore(hasher)
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        api = buildApi(sessionId = null)
    }

    private fun buildApi(sessionId: String?): ApiService {
        val mockInterceptor = MockInterceptor(store, moshi, hasher)
        val client = OkHttpClient.Builder()
            .apply {
                if (sessionId != null) {
                    // Auth header interceptor must run BEFORE MockInterceptor so the
                    // request already carries the header when MockInterceptor reads it.
                    addInterceptor(Interceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("Authorization", "Bearer $sessionId")
                                .build()
                        )
                    })
                }
            }
            .addInterceptor(mockInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://mock.daviplata.local/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    @Test
    fun `login with valid credentials returns session`() = runTest {
        val response = api.login(LoginRequestDto("3001234567", "demo1234"))
        assertTrue("sessionId should not be empty", response.sessionId.isNotEmpty())
        assertEquals("userId should be user-001", "user-001", response.userId)
        assertEquals("name should match", "Fabian Ardila", response.name)
        assertTrue("expiresAt should be in the future", response.expiresAt > System.currentTimeMillis())
    }

    @Test
    fun `login with wrong password returns 401`() = runTest {
        try {
            api.login(LoginRequestDto("3001234567", "wrong"))
            fail("Expected HttpException with 401")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
    }

    @Test
    fun `login with wrong password 3 times locks account`() = runTest {
        repeat(3) {
            try { api.login(LoginRequestDto("3009876543", "wrong")) } catch (_: HttpException) {}
        }
        try {
            api.login(LoginRequestDto("3009876543", "wrong"))
            fail("Expected 423 after 3 failures")
        } catch (e: HttpException) {
            assertEquals(423, e.code())
        }
    }

    @Test
    fun `transfer with insufficient funds returns 409`() = runTest {
        // Login to get a session, then build an authenticated API client
        val session = api.login(LoginRequestDto("3001234567", "demo1234"))
        val authedApi = buildApi(sessionId = session.sessionId)

        try {
            authedApi.createTransfer(TransferRequestDto("3009876543", 9_999_999.0, "Test"))
            fail("Expected 409 INSUFFICIENT_FUNDS")
        } catch (e: HttpException) {
            assertEquals(409, e.code())
        }
    }
}
