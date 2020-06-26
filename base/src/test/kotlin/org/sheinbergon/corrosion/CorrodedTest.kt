package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class CorrodedTest {

    private fun unlatchAndSleep(latch: CountDownLatch) = Runnable {
        latch.countDown()
        runCatching { Thread.sleep(1000L) }
    }

    @Test
    fun `Start a Corroded without an affinity mask`() {
        val defaultAffinity = Corrosion.get()
        val latch = CountDownLatch(1)
        val runnable = unlatchAndSleep(latch)
        val corroded = Corroded(runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity() shouldBeEqualTo defaultAffinity
        } finally {
            corroded.interrupt()
        }
    }
}