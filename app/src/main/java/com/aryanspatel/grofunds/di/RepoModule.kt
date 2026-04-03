package com.aryanspatel.grofunds.di

import com.aryanspatel.grofunds.data.remote.model.FirebaseCurrentUserFlow
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.data.repository.FirebaseAuthRepository
import com.aryanspatel.grofunds.data.remote.model.FirebaseCurrentUserProvider
import com.aryanspatel.grofunds.data.repository.AppScope
import com.aryanspatel.grofunds.data.sync.FirebaseSessionStore
import com.aryanspatel.grofunds.data.sync.SessionStore
import com.aryanspatel.grofunds.data.work.WorkManagerSyncOrchestrator
import com.aryanspatel.grofunds.domain.port.SyncOrchestrator
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.aryanspatel.grofunds.domain.repository.AuthRepository
import com.aryanspatel.grofunds.domain.repository.CurrentUserFlow
import com.aryanspatel.grofunds.domain.repository.CurrentUserProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    abstract fun bindScheduler(impl: WorkManagerSyncOrchestrator) : SyncOrchestrator
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserProviderModule {
    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(
        impl: FirebaseCurrentUserProvider
    ): CurrentUserProvider
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserProviderModule2 {
    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(
        impl: FirebaseCurrentUserFlow
    ): CurrentUserFlow
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: FirebaseSessionStore): SessionStore
}

@Module
@InstallIn(SingletonComponent::class)
object AppScopeModule {
    @Provides
    @Singleton
    @AppScope
    fun appScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}


