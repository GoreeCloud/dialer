package com.goreecloud.dialer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Canonical-package smoke test executed by the managed-device acceptance layer. */
@RunWith(AndroidJUnit4::class)
class DialerApplicationSmokeTest {
    @Test
    fun targetPackageIsCanonicalDialerPackage() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.goreecloud.dialer", targetContext.packageName)
    }
}
