package org.sheinbergon.needle.knitter

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch

class PinnedThreadsTest {

    @Test
    fun `PinnedThread creation - Kotlin interface - Parameterized`() {
        val latch = CountDownLatch(`1`)
        val pinned = pinnedThread(
                start = false,
                name = NEEDLE,
                contextClassLoader = ClassLoader.getPlatformClassLoader(),
                isDaemon = true,
                affinity = testAffinityDescriptor) {
            latch.countDown()
            runCatching { Thread.sleep(1250) }
        }
        pinned.isAlive shouldBe false
        pinned.start()
        latch.await()
        pinned.contextClassLoader shouldBe ClassLoader.getPlatformClassLoader()
        pinned.isDaemon shouldBe true
        pinned.name shouldBeEqualTo NEEDLE
        pinned.affinity().mask() shouldBeEqualTo binaryTestMask
    }

    @Test
    fun `PinnedThread creation - Kotlin interface - Default`() {
        val latch = CountDownLatch(`1`)
        val pinned = pinnedThread {
            latch.countDown()
            runCatching { Thread.sleep(1250) }
        }
        pinned.isAlive shouldBe true
        latch.await()
        pinned.isDaemon shouldBe false
        pinned.affinity().mask() shouldBeEqualTo default.mask()
    }
}
