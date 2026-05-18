package dev.code93.daviplata.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.code93.daviplata.data.repository.*
import dev.code93.daviplata.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun authRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun sessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun accountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds @Singleton
    abstract fun movementRepository(impl: MovementRepositoryImpl): MovementRepository

    @Binds @Singleton
    abstract fun transferRepository(impl: TransferRepositoryImpl): TransferRepository
}
