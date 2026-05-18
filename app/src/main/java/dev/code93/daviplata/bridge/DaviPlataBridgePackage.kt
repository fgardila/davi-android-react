package dev.code93.daviplata.bridge

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.domain.usecase.account.GetBalanceUseCase
import dev.code93.daviplata.domain.usecase.movement.GetMovementsUseCase
import dev.code93.daviplata.domain.usecase.session.ClearSessionUseCase

class DaviPlataBridgePackage(
    private val getBalance: GetBalanceUseCase,
    private val getMovements: GetMovementsUseCase,
    private val clearSession: ClearSessionUseCase,
    private val eventBus: BridgeEventBus,
    private val secureStorage: SecureStorage,
) : ReactPackage {

    override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> =
        listOf(DaviPlataBridgeModule(context, getBalance, getMovements, clearSession, eventBus, secureStorage))

    override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> =
        emptyList()
}
