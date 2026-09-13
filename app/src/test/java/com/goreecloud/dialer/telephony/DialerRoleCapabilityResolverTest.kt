package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialerRoleCapabilityResolverTest {
    private val roleName = "android.app.role.DIALER"
    private val incompleteReason = "Incoming and ongoing call UI acceptance is incomplete"

    @Test
    fun noTelephonyIsUnsupported() {
        assertEquals(
            CapabilityState.Unsupported,
            resolve(hasTelephony = false),
        )
    }

    @Test
    fun missingRoleManagerIsUnavailable() {
        val state = resolve(roleManagerAvailable = false, roleAvailable = false)
        assertTrue(state is CapabilityState.Unavailable)
    }

    @Test
    fun incompleteApplicationDoesNotExposeRoleRequest() {
        assertEquals(
            CapabilityState.Unavailable(incompleteReason),
            resolve(applicationRequirementsAccepted = false),
        )
    }

    @Test
    fun acceptedRequirementsAndUnheldRoleRequiresRole() {
        assertEquals(
            CapabilityState.RoleRequired(roleName),
            resolve(applicationRequirementsAccepted = true),
        )
    }

    @Test
    fun heldRoleReportsPlatformFactEvenWhenAcceptanceIsIncomplete() {
        assertEquals(
            CapabilityState.Active,
            resolve(roleHeld = true, applicationRequirementsAccepted = false),
        )
    }

    private fun resolve(
        hasTelephony: Boolean = true,
        roleManagerAvailable: Boolean = true,
        roleAvailable: Boolean = true,
        roleHeld: Boolean = false,
        applicationRequirementsAccepted: Boolean = false,
    ): CapabilityState = DialerRoleCapabilityResolver.resolve(
        hasTelephony = hasTelephony,
        roleManagerAvailable = roleManagerAvailable,
        roleAvailable = roleAvailable,
        roleHeld = roleHeld,
        roleName = roleName,
        applicationRequirementsAccepted = applicationRequirementsAccepted,
        requirementsReason = incompleteReason,
    )
}
