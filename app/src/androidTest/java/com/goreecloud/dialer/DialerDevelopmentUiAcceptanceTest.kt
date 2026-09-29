package com.goreecloud.dialer

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Executable Development-UI safety checks on a managed Android runtime. */
@RunWith(AndroidJUnit4::class)
class DialerDevelopmentUiAcceptanceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun carrierCallButtonRemainsBlocked() {
        completeFirstUseSetupIfPresent()
        composeRule.onNodeWithText("Call")
            .assertIsNotEnabled()
    }

    @Test
    fun defaultDialerRoleRequestRemainsBlockedByCurrentAcceptance() {
        completeFirstUseSetupIfPresent()
        composeRule.onNodeWithText("Default dialer request blocked")
            .assertIsNotEnabled()
    }

    @Test
    fun incomingCallPresentationAccessIsVisible() {
        completeFirstUseSetupIfPresent()
        composeRule.onNodeWithText("Incoming call presentation access")
            .assertExists()
    }

    @Test
    fun localDialEditorSupportsInternationalPrefixAndClearWithoutEnablingCalls() {
        completeFirstUseSetupIfPresent()

        composeRule.onNodeWithText("+").performClick()
        composeRule.onNodeWithText("1").performClick()
        composeRule.onNodeWithText("2").performClick()
        composeRule.onNodeWithText("3").performClick()

        composeRule.onNodeWithText("+123").assertExists()
        composeRule.onNodeWithText("Call").assertIsNotEnabled()

        composeRule.onNodeWithText("Clear").performClick()
        composeRule.onNodeWithText("Enter a number").assertExists()
        composeRule.onNodeWithText("Call").assertIsNotEnabled()
    }

    private fun completeFirstUseSetupIfPresent() {
        repeat(3) {
            val finishNodes = composeRule
                .onAllNodesWithText("Finish setup")
                .fetchSemanticsNodes()
            if (finishNodes.isNotEmpty()) {
                composeRule.onNodeWithText("Finish setup").performClick()
                composeRule.waitForIdle()
                return
            }

            val continueNodes = composeRule
                .onAllNodesWithText("Continue")
                .fetchSemanticsNodes()
            if (continueNodes.isEmpty()) {
                return
            }
            composeRule.onNodeWithText("Continue").performClick()
            composeRule.waitForIdle()
        }
    }
}
