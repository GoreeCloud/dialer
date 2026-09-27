package com.goreecloud.dialer.guidance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialerGuidanceRepositoryTest {
    @Test
    fun firstUseStartsIncompleteWithHintsEnabled() {
        val repository = DialerGuidanceRepository(FakeStore())
        val state = repository.load()
        assertFalse(state.setupCompleted)
        assertEquals(0, state.setupStep)
        assertTrue(state.hintsEnabled)
        assertTrue(state.dismissedHintIds.isEmpty())
    }

    @Test
    fun setupProgressAndCompletionSurviveRepositoryRecreation() {
        val store = FakeStore()
        val first = DialerGuidanceRepository(store)
        val stepOne = first.nextSetupStep(first.load())
        assertEquals(1, stepOne.setupStep)

        val recreated = DialerGuidanceRepository(store)
        val resumed = recreated.load()
        assertEquals(1, resumed.setupStep)
        assertFalse(resumed.setupCompleted)

        val completed = recreated.completeSetup(resumed)
        assertTrue(completed.setupCompleted)
        assertEquals(DialerGuidanceState.LAST_SETUP_STEP, completed.setupStep)

        val reopened = DialerGuidanceRepository(store).load()
        assertTrue(reopened.setupCompleted)
        assertEquals(DialerGuidanceState.LAST_SETUP_STEP, reopened.setupStep)
    }

    @Test
    fun hintsCanBeDisabledDismissedResetAndReenabled() {
        val repository = DialerGuidanceRepository(FakeStore())
        var state = repository.load()

        state = repository.dismissHint(state, "capability")
        assertFalse(state.isHintVisible("capability"))

        state = repository.resetDismissedHints(state)
        assertTrue(state.isHintVisible("capability"))

        state = repository.setHintsEnabled(state, false)
        assertFalse(state.isHintVisible("capability"))

        state = repository.setHintsEnabled(state, true)
        assertTrue(state.isHintVisible("capability"))
    }

    @Test
    fun replayPreservesGlobalHintPreference() {
        val repository = DialerGuidanceRepository(FakeStore())
        var state = repository.load()
        state = repository.setHintsEnabled(state, false)
        state = repository.completeSetup(state)

        val replay = repository.replaySetup(state)

        assertFalse(replay.setupCompleted)
        assertEquals(0, replay.setupStep)
        assertFalse(replay.hintsEnabled)
    }

    @Test
    fun failedPersistenceDoesNotAdvancePresentedState() {
        val store = FakeStore(acceptWrites = false)
        val repository = DialerGuidanceRepository(store)
        val initial = repository.load()

        val attempted = repository.nextSetupStep(initial)

        assertEquals(initial, attempted)
        assertEquals(null, store.stored)
    }

    private class FakeStore(
        var stored: DialerGuidanceState? = null,
        var acceptWrites: Boolean = true,
    ) : DialerGuidanceStore {
        override fun read(): DialerGuidanceState? = stored

        override fun write(state: DialerGuidanceState): Boolean {
            if (!acceptWrites) return false
            stored = state
            return true
        }
    }
}
