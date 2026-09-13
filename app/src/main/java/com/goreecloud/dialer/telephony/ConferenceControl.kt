package com.goreecloud.dialer.telephony

sealed interface ConferenceControlAction {
    data class ConferenceWith(val otherSessionId: Long) : ConferenceControlAction
    data object MergeConference : ConferenceControlAction
    data object SwapConference : ConferenceControlAction
    data object SeparateFromConference : ConferenceControlAction
}

sealed interface ConferenceControlResult {
    data object Submitted : ConferenceControlResult
    data class Rejected(val reason: String) : ConferenceControlResult
    data class Failed(val reason: String) : ConferenceControlResult
}

data class ConferenceControlFacts(
    val sourceSessionId: Long,
    val state: CallLifecycleState,
    val conferenceableSessionIds: Set<Long>,
    val hasParent: Boolean,
    val mergeConferenceAvailable: Boolean,
    val swapConferenceAvailable: Boolean,
    val separateFromConferenceAvailable: Boolean,
)

sealed interface ConferenceControlDecision {
    data object Allowed : ConferenceControlDecision
    data class Rejected(val reason: String) : ConferenceControlDecision
}

/**
 * Pure conference authorization policy built only from live Telecom evidence.
 *
 * A conference operation is never inferred from call count alone. Pairwise conferencing must be
 * explicitly listed by Telecom, while conference merge/swap/separate operations require the
 * corresponding live capability bit.
 */
object ConferenceControlPolicy {
    fun decide(
        action: ConferenceControlAction,
        facts: ConferenceControlFacts,
    ): ConferenceControlDecision {
        if (facts.state !in CONFERENCE_STATES) {
            return ConferenceControlDecision.Rejected(
                "Conference controls are unavailable while call state is ${facts.state}",
            )
        }

        return when (action) {
            is ConferenceControlAction.ConferenceWith -> when {
                action.otherSessionId == facts.sourceSessionId ->
                    ConferenceControlDecision.Rejected("A call cannot conference with itself")

                action.otherSessionId !in facts.conferenceableSessionIds ->
                    ConferenceControlDecision.Rejected(
                        "Android Telecom does not list that call as conferenceable",
                    )

                else -> ConferenceControlDecision.Allowed
            }

            ConferenceControlAction.MergeConference ->
                if (facts.mergeConferenceAvailable) {
                    ConferenceControlDecision.Allowed
                } else {
                    ConferenceControlDecision.Rejected(
                        "Android Telecom does not currently allow this conference to merge",
                    )
                }

            ConferenceControlAction.SwapConference ->
                if (facts.swapConferenceAvailable) {
                    ConferenceControlDecision.Allowed
                } else {
                    ConferenceControlDecision.Rejected(
                        "Android Telecom does not currently allow this conference to swap",
                    )
                }

            ConferenceControlAction.SeparateFromConference -> when {
                !facts.hasParent ->
                    ConferenceControlDecision.Rejected(
                        "This call is not currently attached to a tracked conference",
                    )

                !facts.separateFromConferenceAvailable ->
                    ConferenceControlDecision.Rejected(
                        "Android Telecom does not currently allow this call to separate",
                    )

                else -> ConferenceControlDecision.Allowed
            }
        }
    }

    private val CONFERENCE_STATES = setOf(
        CallLifecycleState.ACTIVE,
        CallLifecycleState.HOLDING,
    )
}
