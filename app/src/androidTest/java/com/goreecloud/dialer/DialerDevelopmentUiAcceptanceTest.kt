package com.goreecloud.dialer

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithText("Call")
            .assertIsNotEnabled()
    }

    @Test
    fun defaultDialerRoleRequestRemainsBlockedByCurrentAcceptance() {
        composeRule.onNodeWithText("Default dialer request blocked")
            .assertIsNotEnabled()
    }

    @Test
    fun incomingCallPresentationAccessIsVisible() {
        composeRule.onNodeWithText("Incoming call presentation access")
            .assertExists()
    }
}
