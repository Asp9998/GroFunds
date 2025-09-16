package com.aryanspatel.grofunds.di

import com.aryanspatel.grofunds.common.DefaultDispatcherProvider
import com.aryanspatel.grofunds.common.DispatcherProvider
import com.aryanspatel.grofunds.data.repository.AuthRepository
import com.aryanspatel.grofunds.data.repository.UserRepository
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    // Provide Dispatcher Provider
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()


    // Provide FirebaseAuth instance
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Provide FireStore instance
    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // offline cache
            .build()
        return db
    }

    // Provide AuthRepository (depends on FirebaseAuth)
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        dp: DispatcherProvider
    ): AuthRepository = AuthRepository(firebaseAuth, dp)


    // Provide UserRepository (depends on FirebaseAuth and Firebase FireStore
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        dp: DispatcherProvider
    ): UserRepository = UserRepository(firebaseAuth, firestore, dp)

    // Provide AddEntryEntry Repository (depends on Firebase Auth and Firebase FireStore)
    @Provides
    @Singleton
    fun providesAddEntryRepository(
        firebaseAuth: FirebaseAuth,
        firebaseStore: FirebaseFirestore,
        dp: DispatcherProvider
    ): AddEntryRepository = AddEntryRepository(firebaseAuth, firebaseStore, dp)

}