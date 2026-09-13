package com.goreecloud.dialer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation foundation smoke test.
 *
 * CI compiles this test APK; executing it on an emulator or physical device remains a separate
 * acceptance step and must not be inferred from successful assembly.
 */
@RunWith(AndroidJUnit4::class)
class DialerApplicationSmokeTest {
    @Test
    fun targetPackageIsCanonicalDialerPackage() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.goreecloud.dialer", targetContext.packageName)
    }
}
