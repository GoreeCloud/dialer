package com.goreecloud.dialer.guidance

import android.content.Context

data class DialerGuidanceState(
    val setupCompleted: Boolean = false,
    val setupStep: Int = 0,
    val hintsEnabled: Boolean = true,
    val dismissedHintIds: Set<String> = emptySet(),
) {
    init {
        require(setupStep in 0..LAST_SETUP_STEP) {
            "setupStep must be between 0 and $LAST_SETUP_STEP"
        }
    }

    fun isHintVisible(hintId: String): Boolean =
        hintsEnabled && hintId !in dismissedHintIds

    companion object {
        const val LAST_SETUP_STEP = 2
    }
}

interface DialerGuidanceStore {
    fun read(): DialerGuidanceState?
    fun write(state: DialerGuidanceState): Boolean
}

/**
 * Durable, fail-closed state machine for Dialer's mandatory first-use guidance.
 *
 * Guidance state is presentation-only. It never grants Android permissions, roles, Telecom
 * capability, carrier availability, or call authority.
 */
class DialerGuidanceRepository(
    private val store: DialerGuidanceStore,
) {
    fun load(): DialerGuidanceState =
        store.read() ?: DialerGuidanceState()

    fun nextSetupStep(current: DialerGuidanceState): DialerGuidanceState {
        if (current.setupCompleted) return current
        return persist(
            current,
            current.copy(
                setupStep = (current.setupStep + 1).coerceAtMost(DialerGuidanceState.LAST_SETUP_STEP),
            ),
        )
    }

    fun previousSetupStep(current: DialerGuidanceState): DialerGuidanceState {
        if (current.setupCompleted) return current
        return persist(
            current,
            current.copy(setupStep = (current.setupStep - 1).coerceAtLeast(0)),
        )
    }

    fun completeSetup(current: DialerGuidanceState): DialerGuidanceState =
        persist(
            current,
            current.copy(
                setupCompleted = true,
                setupStep = DialerGuidanceState.LAST_SETUP_STEP,
            ),
        )

    fun replaySetup(current: DialerGuidanceState): DialerGuidanceState =
        persist(
            current,
            current.copy(
                setupCompleted = false,
                setupStep = 0,
            ),
        )

    fun setHintsEnabled(
        current: DialerGuidanceState,
        enabled: Boolean,
    ): DialerGuidanceState =
        persist(current, current.copy(hintsEnabled = enabled))

    fun dismissHint(
        current: DialerGuidanceState,
        hintId: String,
    ): DialerGuidanceState {
        if (hintId.isBlank()) return current
        return persist(
            current,
            current.copy(dismissedHintIds = current.dismissedHintIds + hintId),
        )
    }

    fun resetDismissedHints(current: DialerGuidanceState): DialerGuidanceState =
        persist(current, current.copy(dismissedHintIds = emptySet()))

    private fun persist(
        current: DialerGuidanceState,
        next: DialerGuidanceState,
    ): DialerGuidanceState =
        if (store.write(next)) next else current
}

class SharedPreferencesDialerGuidanceStore(
    context: Context,
) : DialerGuidanceStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override fun read(): DialerGuidanceState? {
        if (!preferences.contains(KEY_SCHEMA_VERSION)) return null

        if (preferences.getInt(KEY_SCHEMA_VERSION, -1) != SCHEMA_VERSION) {
            // Unknown persisted state must not silently mark setup complete or turn hints back on.
            return DialerGuidanceState(
                setupCompleted = false,
                setupStep = 0,
                hintsEnabled = false,
            )
        }

        return DialerGuidanceState(
            setupCompleted = preferences.getBoolean(KEY_SETUP_COMPLETED, false),
            setupStep = preferences.getInt(KEY_SETUP_STEP, 0)
                .coerceIn(0, DialerGuidanceState.LAST_SETUP_STEP),
            hintsEnabled = preferences.getBoolean(KEY_HINTS_ENABLED, true),
            dismissedHintIds = preferences.getStringSet(KEY_DISMISSED_HINT_IDS, emptySet())
                ?.toSet()
                .orEmpty(),
        )
    }

    override fun write(state: DialerGuidanceState): Boolean =
        preferences.edit()
            .putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            .putBoolean(KEY_SETUP_COMPLETED, state.setupCompleted)
            .putInt(KEY_SETUP_STEP, state.setupStep)
            .putBoolean(KEY_HINTS_ENABLED, state.hintsEnabled)
            .putStringSet(KEY_DISMISSED_HINT_IDS, state.dismissedHintIds.toSet())
            .commit()

    private companion object {
        const val PREFERENCES_NAME = "goreecloud_dialer_guidance"
        const val SCHEMA_VERSION = 1
        const val KEY_SCHEMA_VERSION = "schema_version"
        const val KEY_SETUP_COMPLETED = "setup_completed"
        const val KEY_SETUP_STEP = "setup_step"
        const val KEY_HINTS_ENABLED = "hints_enabled"
        const val KEY_DISMISSED_HINT_IDS = "dismissed_hint_ids"
    }
}
