package com.aryanspatel.grofunds.fakeRepo.test

import app.cash.turbine.test
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.domain.model.ParseState
import com.aryanspatel.grofunds.domain.model.ParsedEntry
import com.aryanspatel.grofunds.fakeRepo.repository.FakeAddEntryRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeAddEntryRepositoryTest{
    private fun repo() = FakeAddEntryRepository(uid = "USER_TEST")

    @Test
    fun createDraft_returnsRef_and_emitsPending() = runTest {
        val r = repo()
        val ref = r.createDraft(
            kind = EntryKind.EXPENSE,
            note = "coffee 5.4 CAD",
            currencyHint = "CAD",
            localeHint = "en-CA",
            timeZone = "America/Winnipeg"
        )

        assertThat(ref.kind).isEqualTo(EntryKind.EXPENSE)
        assertThat(ref.id).startsWith("DRAFT_")
        assertThat(ref.path).isEqualTo("users/USER_TEST/expenses/${ref.id}")

        // Observe: first emission should be Pending(note)
        r.observe(ref.path).test {
            val first = awaitItem()
            assertThat(first).isInstanceOf(ParseState.Pending::class.java)
            val note = (first as ParseState.Pending).note
            assertThat(note).isEqualTo("coffee 5.4 CAD")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createDraft_filtersNullHints_in_client_and_survives_after_save() = runTest {
        val r = repo()

        // Provide only currencyHint; others null
        val ref = r.createDraft(
            kind = EntryKind.EXPENSE,
            note = "coffee 4.50 CAD",
            currencyHint = "CAD",
            localeHint = null,
            timeZone = null
        )

        // Start observing BEFORE save to get both Pending and Ready
        r.observe(ref.path).test {
            assertThat(awaitItem()).isInstanceOf(ParseState.Pending::class.java)

            // Now save → fake will merge + status = saved → Ready(map)
            r.saveExpense(
                ref.path,
                ParsedEntry.Expense(
                    amount = 4.5,
                    currency = "cad",
                    category = "food",
                    subcategory = "coffee",
                    merchant = "Tim's",
                    notes = "morning",
                    dateText = "TEST_DATE",
                    confidence = 0.0
                )
            )

            val ready = awaitItem()
            assertThat(ready).isInstanceOf(ParseState.Ready::class.java)
            val data = (ready as ParseState.Ready).data

            // input retained, status updated, _client contains ONLY provided hints
            assertThat(data["input"]).isEqualTo("coffee 4.50 CAD")
            assertThat(data["status"]).isEqualTo("saved")
            @Suppress("UNCHECKED_CAST")
            val client = data["_client"] as Map<String, Any?>
            assertThat(client.keys).containsExactly("currencyHint")
            assertThat(client["currencyHint"]).isEqualTo("CAD")

//             saved fields present
            assertThat(data["amount"]).isEqualTo(4.5)
            assertThat(data["currency"]).isEqualTo("CAD") // fake uppercases
            assertThat(data["category"]).isEqualTo("food")
            assertThat(data["merchant"]).isEqualTo("Tim's")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveIncome_transitions_toReady_with_expected_fields() = runTest {
        val r = repo()
        val ref = r.createDraft(
            kind = EntryKind.INCOME,
            note = "salary Aug",
            currencyHint = null, localeHint = null, timeZone = null
        )

        r.observe(ref.path).test {
            assertThat(awaitItem()).isInstanceOf(ParseState.Pending::class.java)

            r.saveIncome(
                ref.path,
                ParsedEntry.Income(
                    amount = 1200.0,
                    currency = "cad",
                    type = "salary",
                    dateText = "2025-08-31",
                    notes = "net"
                )
            )

            val ready = awaitItem() as ParseState.Ready
            assertThat(ready.data["status"]).isEqualTo("saved")
            assertThat(ready.data["amount"]).isEqualTo(1200.0)
            assertThat(ready.data["currency"]).isEqualTo("CAD")
            assertThat(ready.data["type"]).isEqualTo("salary")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveGoal_transitions_toReady_with_expected_fields() = runTest {
        val r = repo()
        val ref = r.createDraft(
            kind = EntryKind.GOAL,
            note = "save 5k by Dec",
            currencyHint = "CAD", localeHint = "en-CA", timeZone = null
        )

        r.observe(ref.path).test {
            assertThat(awaitItem()).isInstanceOf(ParseState.Pending::class.java)

            r.saveGoal(
                ref.path,
                ParsedEntry.Goal(
                    title = "Emergency fund",
                    type = "saving",
                    amount = 5000.0,
                    currency = "cad",
                    dueDate = "2025-12-31",
                    startAmount = 1000.0,
                    dateText = "2025-09-01",
                    notes = "aggressive"
                )
            )

            val ready = awaitItem() as ParseState.Ready
            assertThat(ready.data["status"]).isEqualTo("saved")
            assertThat(ready.data["title"]).isEqualTo("Emergency fund")
            assertThat(ready.data["amount"]).isEqualTo(5000.0)
            assertThat(ready.data["currency"]).isEqualTo("CAD")
            assertThat(ready.data["dueDate"]).isEqualTo("2025-12-31")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteIfNotSaved_removes_pending_but_keeps_saved() = runTest {
        val r = repo()

        // Pending draft → should delete
        val d1 = r.createDraft(EntryKind.EXPENSE, "coffee", null, null, null)
        r.observe(d1.path).test {
            assertThat(awaitItem()).isInstanceOf(ParseState.Pending::class.java)
            r.deleteIfNotSaved(d1.kind, d1.id)
            val after = awaitItem()
            assertThat(after).isInstanceOf(ParseState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }

        // Saved draft → should NOT delete
        val d2 = r.createDraft(EntryKind.INCOME, "salary", null, null, null)
        // save to mark as saved
        r.saveIncome(d2.path, ParsedEntry.Income(100.0, "cad", "salary", "2025-09-01", null))

        // Try delete; stream should stay Ready (no error emission)
        r.observe(d2.path).test {
            // initial will be Ready because already saved
            val initial = awaitItem()
            assertThat(initial).isInstanceOf(ParseState.Ready::class.java)
            r.deleteIfNotSaved(d2.kind, d2.id)

            // no change expected; ensure no Error emitted within a short window
            expectNoEvents() // turbine helper (uses default timeout)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observe_missing_path_emitsError() = runTest {
        val r = repo()
        r.observe("users/U_TEST/expenses/NOPE").test {
            val first = awaitItem()
            assertThat(first).isInstanceOf(ParseState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

}