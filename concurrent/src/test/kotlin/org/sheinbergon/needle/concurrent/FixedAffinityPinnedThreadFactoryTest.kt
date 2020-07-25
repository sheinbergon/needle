package org.sheinbergon.needle.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch

class FixedAffinityPinnedThreadFactoryTest {

    @Test
    fun `Initialize the factory`() {
        val factory =
            FixedAffinityPinnedThreadFactory(testAffinityDescriptor)
        testPinnedThreadFactory(factory)
    }


    private fun testPinnedThreadFactory(factory: PinnedThreadFactory) {
        val latch = CountDownLatch(`1`)
        val pinned = factory.newThread(task(latch))
        pinned.start()
        latch.await()
    }

    private fun task(latch: CountDownLatch) = Runnable {
        val self = Thread.currentThread() as PinnedThread
        self.affinity().mask() shouldBeEqualTo binaryTestMask
        self.affinity().toString() shouldBeEqualTo textTestMask
        latch.countDown()
    }
}
