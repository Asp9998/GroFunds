package com.aryanspatel.grofunds

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aryanspatel.grofunds.core.DispatcherProvider
import com.aryanspatel.grofunds.data.repository.AddEntryTransactionRepository
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.repository.AddEntryRepository
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import app.cash.turbine.test


@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddEntryRepositoryEmulatorTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private val dp = object : DispatcherProvider {
        override val iO = dispatcher
        override val default = dispatcher
        override val unconfined = dispatcher
        override val main = dispatcher
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var repo: AddEntryRepository

    @Before
    fun setUp() = runTest(scheduler) {
        Dispatchers.setMain(dispatcher)
        db = FirebaseFirestore.getInstance().apply {
            useEmulator("10.0.2.2", 8080)
            firestoreSettings = firestoreSettings {
                isPersistenceEnabled = false
            }
        }
        auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }
        auth.signInAnonymously().await()   // ensure currentUser
        repo = AddEntryTransactionRepository(auth, db, dp)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun createDraft_emitsPending_and_pathStable() = runTest(scheduler) {
        val d = repo.createDraft(EntryKind.EXPENSE, "coffee", "CAD", "en-CA", "America/Winnipeg")
        // path structure
        val parts = d.path.split('/')
        assertThat(parts).containsExactly("users", auth.currentUser!!.uid, "expenses", d.id)
            .inOrder()

        repo.observe(d.path).test {
            assertThat(awaitItem()).isInstanceOf(ParseState.Pending::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // Add 4–6 more tests mirroring your fake tests.

}