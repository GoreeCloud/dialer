package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionInventoryGatewayTest {
    @Test
    fun permissionIsRequiredBeforePlatformInventoryIsRead() {
        val platform = FakePlatform(
            permissionGranted = false,
            read = PlatformSubscriptionRead.Available(listOf(1)),
        )

        assertEquals(
            SubscriptionInventoryResult.PermissionRequired,
            SubscriptionInventoryGateway(platform).read(),
        )
        assertEquals(0, platform.readCount)
    }

    @Test
    fun availableIdsAreMinimizedDeduplicatedAndSorted() {
        val platform = FakePlatform(
            permissionGranted = true,
            read = PlatformSubscriptionRead.Available(listOf(7, -1, 3, 7, 5)),
        )

        assertEquals(
            SubscriptionInventoryResult.Available(
                listOf(
                    ActiveSubscription(3),
                    ActiveSubscription(5),
                    ActiveSubscription(7),
                )
            ),
            SubscriptionInventoryGateway(platform).read(),
        )
        assertEquals(1, platform.readCount)
    }

    @Test
    fun permissionRevocationBetweenPreflightAndReadFailsClosed() {
        val platform = FakePlatform(
            permissionGranted = true,
            read = PlatformSubscriptionRead.PermissionDenied,
        )

        assertEquals(
            SubscriptionInventoryResult.PermissionRequired,
            SubscriptionInventoryGateway(platform).read(),
        )
    }

    @Test
    fun unsupportedRuntimeRemainsExplicit() {
        assertEquals(
            SubscriptionInventoryResult.Unsupported,
            SubscriptionInventoryGateway(
                FakePlatform(
                    permissionGranted = true,
                    read = PlatformSubscriptionRead.Unsupported,
                )
            ).read(),
        )
    }

    @Test
    fun platformFailureDoesNotInventAnEmptySubscriptionSet() {
        assertEquals(
            SubscriptionInventoryResult.Unavailable("IllegalStateException"),
            SubscriptionInventoryGateway(
                FakePlatform(
                    permissionGranted = true,
                    read = PlatformSubscriptionRead.Failed("IllegalStateException"),
                )
            ).read(),
        )
    }

    @Test
    fun successfulEmptyInventoryIsDistinctFromReadFailure() {
        assertEquals(
            SubscriptionInventoryResult.Available(emptyList()),
            SubscriptionInventoryGateway(
                FakePlatform(
                    permissionGranted = true,
                    read = PlatformSubscriptionRead.Available(emptyList()),
                )
            ).read(),
        )
    }

    private class FakePlatform(
        private val permissionGranted: Boolean,
        private val read: PlatformSubscriptionRead,
    ) : SubscriptionInventoryPlatform {
        var readCount: Int = 0
            private set

        override fun hasReadPhoneStatePermission(): Boolean = permissionGranted

        override fun activeSubscriptionIds(): PlatformSubscriptionRead {
            readCount += 1
            return read
        }
    }
}
