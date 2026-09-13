package com.goreecloud.dialer.telephony

enum class DefaultDialerRequirement(val displayName: String) {
    ACTION_DIAL("ACTION_DIAL handling"),
    IN_CALL_SERVICE("InCallService lifecycle"),
    OUTGOING_CALL_PLACEMENT("outgoing call placement"),
    INCOMING_CALL_UI("incoming call UI"),
    ONGOING_CALL_UI("ongoing call UI"),
}

/**
 * GoreeCloud's explicit acceptance gate for requesting Android's default-dialer role.
 *
 * Platform eligibility and GoreeCloud acceptance are deliberately separate. A source path can
 * exist without being accepted; role requests stay closed until every required path is verified.
 */
data class DefaultDialerAcceptance(
    val actionDialAccepted: Boolean,
    val inCallServiceAccepted: Boolean,
    val outgoingCallPlacementAccepted: Boolean,
    val incomingCallUiAccepted: Boolean,
    val ongoingCallUiAccepted: Boolean,
) {
    val missingRequirements: List<DefaultDialerRequirement>
        get() = buildList {
            if (!actionDialAccepted) add(DefaultDialerRequirement.ACTION_DIAL)
            if (!inCallServiceAccepted) add(DefaultDialerRequirement.IN_CALL_SERVICE)
            if (!outgoingCallPlacementAccepted) add(DefaultDialerRequirement.OUTGOING_CALL_PLACEMENT)
            if (!incomingCallUiAccepted) add(DefaultDialerRequirement.INCOMING_CALL_UI)
            if (!ongoingCallUiAccepted) add(DefaultDialerRequirement.ONGOING_CALL_UI)
        }

    val accepted: Boolean
        get() = missingRequirements.isEmpty()

    fun unavailableReason(): String = if (accepted) {
        "Default-dialer application requirements accepted"
    } else {
        "Default-dialer acceptance incomplete: " +
            missingRequirements.joinToString { it.displayName }
    }
}

/**
 * Current verified Development acceptance state. Change a field only when its end-to-end runtime
 * path and required validation are complete; source presence alone is not sufficient.
 */
object DevelopmentDefaultDialerAcceptance {
    val current = DefaultDialerAcceptance(
        actionDialAccepted = true,
        inCallServiceAccepted = true,
        outgoingCallPlacementAccepted = false,
        incomingCallUiAccepted = false,
        ongoingCallUiAccepted = false,
    )
}
