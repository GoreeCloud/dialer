package com.goreecloud.dialer.telephony

sealed interface PostDialControlAction {
    data object Continue : PostDialControlAction
    data object Cancel : PostDialControlAction
}

sealed interface PostDialControlResult {
    data object Submitted : PostDialControlResult
    data class Rejected(val reason: String) : PostDialControlResult
    data class Failed(val reason: String) : PostDialControlResult
}

data class PostDialControlFacts(
    val state: CallLifecycleState,
    val waitPending: Boolean,
)

sealed interface PostDialControlDecision {
    data object Allowed : PostDialControlDecision
    data class Rejected(val reason: String) : PostDialControlDecision
}

/** User confirmation policy for Android's post-dial wait callback. */
object PostDialControlPolicy {
    fun decide(facts: PostDialControlFacts): PostDialControlDecision = when {
        !facts.waitPending ->
            PostDialControlDecision.Rejected("No post-dial confirmation is currently pending")

        facts.state !in SUPPORTED_STATES ->
            PostDialControlDecision.Rejected(
                "Post-dial confirmation is unavailable while call state is ${facts.state}",
            )

        else -> PostDialControlDecision.Allowed
    }

    private val SUPPORTED_STATES = setOf(
        CallLifecycleState.CONNECTING,
        CallLifecycleState.DIALING,
        CallLifecycleState.ACTIVE,
        CallLifecycleState.HOLDING,
    )
}
