package dev.code93.daviplata.bridge

import com.facebook.react.bridge.*
import dev.code93.daviplata.BuildConfig
import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.usecase.account.GetBalanceUseCase
import dev.code93.daviplata.domain.usecase.movement.GetMovementsUseCase
import dev.code93.daviplata.domain.usecase.session.ClearSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DaviPlataBridgeModule(
    reactContext: ReactApplicationContext,
    private val getBalance: GetBalanceUseCase,
    private val getMovements: GetMovementsUseCase,
    private val clearSession: ClearSessionUseCase,
    private val eventBus: BridgeEventBus,
    private val secureStorage: SecureStorage,
) : ReactContextBaseJavaModule(reactContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getName() = "DaviPlataBridge"

    override fun initialize() {
        super.initialize()
        eventBus.attach(reactApplicationContext)
    }

    @ReactMethod
    fun getBalance(promise: Promise) {
        scope.launch {
            when (val result = getBalance().first { it !is ApiResult.Loading }) {
                is ApiResult.Success -> {
                    val map = Arguments.createMap().apply {
                        putDouble("amount", result.data.amount)
                        putString("currency", result.data.currency)
                    }
                    promise.resolve(map)
                }
                is ApiResult.Failure ->
                    promise.reject("GET_BALANCE_ERROR", result.error.message ?: result.error::class.simpleName)
                ApiResult.Loading -> Unit
            }
        }
    }

    @ReactMethod
    fun getMovements(page: Int, size: Int, promise: Promise) {
        scope.launch {
            when (val result = getMovements(page, size).first { it !is ApiResult.Loading }) {
                is ApiResult.Success -> {
                    val page_ = result.data
                    val array = Arguments.createArray()
                    page_.items.forEach { m ->
                        Arguments.createMap().apply {
                            putString("id", m.id)
                            putString("type", m.type.name)
                            putString("status", m.status.name)
                            putDouble("amount", m.amount)
                            putString("description", m.description)
                            putDouble("occurredAtMillis", m.occurredAtMillis.toDouble())
                        }.also { array.pushMap(it) }
                    }
                    val map = Arguments.createMap().apply {
                        putArray("items", array)
                        putInt("total", page_.total)
                        putInt("page", page_.page)
                    }
                    promise.resolve(map)
                }
                is ApiResult.Failure ->
                    promise.reject("GET_MOVEMENTS_ERROR", result.error.message ?: result.error::class.simpleName)
                ApiResult.Loading -> Unit
            }
        }
    }

    @ReactMethod
    fun openTransfer(payload: ReadableMap) {
        val intent = android.content.Intent(
            reactApplicationContext,
            Class.forName("dev.code93.daviplata.presentation.transfer.TransferActivity"),
        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }

    @ReactMethod
    fun logout(promise: Promise) {
        scope.launch {
            runCatching { clearSession.invoke() }
                .onSuccess {
                    val intent = android.content.Intent(
                        reactApplicationContext,
                        Class.forName("dev.code93.daviplata.presentation.DaviPlataActivity"),
                    ).addFlags(
                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    )
                    reactApplicationContext.startActivity(intent)
                    promise.resolve(null)
                }
                .onFailure { promise.reject("LOGOUT_ERROR", it.message, it) }
        }
    }

    @ReactMethod
    fun forceSessionExpired(promise: Promise) {
        if (!BuildConfig.DEBUG_TOOLS) { promise.reject("FORBIDDEN", "Debug tools disabled"); return }
        secureStorage.setExpiresAt(0L)
        promise.resolve(null)
    }

    // Suppress RN warnings for addListener/removeListeners
    @ReactMethod fun addListener(@Suppress("UNUSED_PARAMETER") eventName: String) {}
    @ReactMethod fun removeListeners(@Suppress("UNUSED_PARAMETER") count: Int) {}
}
