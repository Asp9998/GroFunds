package com.aryanspatel.grofunds.di

import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.data.repository.FirebaseAuthRepository
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AuthRepoModule {
    @Binds
    @Singleton
    fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface AddEntryRepoModule{
    @Binds
    @Singleton
    fun bindAddEntryRepository(
        impl: AddEntryTransactionRepository
    ): AddEntryRepository
}

