package org.sheinbergon.corrosion

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import org.amshove.kluent.`should be equal to`
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class CorrosionTests {

    @Test
    fun `set affinity for a JVM Thread using a mask`() {
        val expected: Long = 0x0000000000000003
        val actual: AtomicLong = atomic(0x1111111111111111)
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

    @Test
    fun `set affinity for a JVM Thread using a core list`() {
        val expected = "3"
        val actual = atomic(StringUtils.EMPTY)
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
            actual: AtomicRef<String>,
            expected: String): () -> Unit = {
        Corrosion.set(expected)
        actual.value = Corrosion.get().toString()
        latch.countDown()
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