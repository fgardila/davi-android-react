package dev.code93.daviplata.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// SecureStorage usa @Singleton @Inject constructor directamente — no requiere @Provides.
// Este módulo existe como placeholder para dependencias de almacenamiento futuras.
@Module
@InstallIn(SingletonComponent::class)
object StorageModule
