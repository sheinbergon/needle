package org.sheinbergon.corrosion.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.*
import java.util.concurrent.CountDownLatch

class FixedAffinityCorrodedFactoryTest {

    @Test
    fun `Initialize the factory using a binary mask`() {
        val factory = FixedAffinityCorrodedFactory(binaryTestMask)
        testCorrodedFactory(factory)
    }

    @Test
    fun `Initialize the factory using a text mask`() {
        val factory = FixedAffinityCorrodedFactory(textTestMask)
        testCorrodedFactory(factory)
    }

    private fun testCorrodedFactory(factory: CorrodedFactory) {
        val latch = CountDownLatch(`1`)
        val corroded = factory.newThread(task(latch))
        corroded.start()
        latch.await()
    }

    private fun task(latch: CountDownLatch) = Runnable {
        val self = Thread.currentThread() as Corroded
        self.affinity().mask() shouldBeEqualTo binaryTestMask
        self.affinity().toString() shouldBeEqualTo textTestMask
        latch.countDown()
    }
}