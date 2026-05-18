package dev.code93.daviplata.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.code93.daviplata.bridge.BridgeEventBus
import dev.code93.daviplata.data.local.SecureStorage
import dev.code93.daviplata.domain.usecase.account.GetBalanceUseCase
import dev.code93.daviplata.domain.usecase.movement.GetMovementsUseCase
import dev.code93.daviplata.domain.usecase.session.ClearSessionUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BridgeEntryPoint {
    fun getBalanceUseCase(): GetBalanceUseCase
    fun getMovementsUseCase(): GetMovementsUseCase
    fun clearSessionUseCase(): ClearSessionUseCase
    fun bridgeEventBus(): BridgeEventBus
    fun secureStorage(): SecureStorage
}
