package org.sheinbergon.corrosion

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class CorrosionTests {

    @Test
    fun `set affinity for a JVM Thread`() {
        val expected = 3L
        val actual = atomic(-1L)
        val latch = CountDownLatch(1)
        val thread = thread(start = false, block = setAndGetAffinityRunnable(latch, actual, expected))
        try {
            thread.start()
            latch.await()
            actual.value `should be equal to` expected
        } finally {
            thread.interrupt()
        }
    }

    private fun setAndGetAffinityRunnable(
            latch: CountDownLatch,
            actual: AtomicLong,
            expected: Long): () -> Unit = {
        Corrosion.set(expected)
        actual.value = Corrosion.get().mask()
        latch.countDown()
    }
}