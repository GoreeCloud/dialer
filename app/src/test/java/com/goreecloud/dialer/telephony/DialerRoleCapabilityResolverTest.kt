package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialerRoleCapabilityResolverTest {
    private val roleName = "android.app.role.DIALER"

    @Test
    fun noTelephonyIsUnsupported() {
        assertEquals(
            CapabilityState.Unsupported,
            DialerRoleCapabilityResolver.resolve(
                hasTelephony = false,
                roleManagerAvailable = true,
                roleAvailable = true,
                roleHeld = false,
                roleName = roleName,
            ),
        )
    }

    @Test
    fun missingRoleManagerIsUnavailable() {
        val state = DialerRoleCapabilityResolver.resolve(
            hasTelephony = true,
            roleManagerAvailable = false,
            roleAvailable = false,
            roleHeld = false,
            roleName = roleName,
        )
        assertTrue(state is CapabilityState.Unavailable)
    }

    @Test
    fun availableRoleNotHeldRequiresRole() {
        assertEquals(
            CapabilityState.RoleRequired(roleName),
            DialerRoleCapabilityResolver.resolve(
                hasTelephony = true,
                roleManagerAvailable = true,
                roleAvailable = true,
                roleHeld = false,
                roleName = roleName,
            ),
        )
    }

    @Test
    fun heldRoleIsActive() {
        assertEquals(
            CapabilityState.Active,
            DialerRoleCapabilityResolver.resolve(
                hasTelephony = true,
                roleManagerAvailable = true,
                roleAvailable = true,
                roleHeld = true,
                roleName = roleName,
            ),
        )
    }
}
