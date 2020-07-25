package org.sheinbergon.needle

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.util.NeedleException
import java.util.concurrent.CountDownLatch

class PinnedThreadTest {

    private lateinit var latch: CountDownLatch

    @BeforeEach
    fun setup() {
        latch = CountDownLatch(1)
    }

    @Test
    fun `Access a PinnedThread affinity information without starting it`() {
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable)
        Assertions.assertThrows(NeedleException::class.java) { pinned.affinity() }
    }

    @Test
    fun `Start a PinnedThread without an affinity descriptor`() {
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable)
        try {
            pinned.start()
            latch.await()
            pinned.affinity() shouldBeEqualTo default
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Start a named PinnedThread without an affinity descriptor`() {
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable, NEEDLE)
        try {
            pinned.start()
            pinned.name shouldBeEqualTo NEEDLE
            latch.await()
            pinned.affinity() shouldBeEqualTo default
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Start a PinnedThread with an affinity descriptor set`() {
        val desiredMask = textTestMask
        val desiredAffinityDescriptor = testAffinityDescriptor
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable, desiredAffinityDescriptor)
        try {
            pinned.start()
            latch.await()
            pinned.affinity().toString() shouldBeEqualTo desiredMask
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Start a named PinnedThread with an affinity descriptor set`() {
        val desiredMask = textTestMask
        val desiredAffinityDescriptor = testAffinityDescriptor
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable, NEEDLE, desiredAffinityDescriptor)
        try {
            pinned.start()
            pinned.name shouldBeEqualTo NEEDLE
            latch.await()
            pinned.affinity().toString() shouldBeEqualTo desiredMask
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Change the affinity of a PinnedThread during runtime`() {
        val desiredTextMask = textTestMask
        val desiredBinaryMask = binaryTestMask
        val desiredAffinityDescriptor = testAffinityDescriptor
        val runnable = unlatchAndSleepRunnable()
        val pinned = PinnedThread(runnable)
        try {
            pinned.start()
            latch.await()
            pinned.affinity() shouldBeEqualTo default
            pinned.affinity(desiredAffinityDescriptor)
            pinned.affinity() shouldNotBeEqualTo default
            pinned.affinity().mask() shouldBeEqualTo desiredBinaryMask
            pinned.affinity().toString() shouldBeEqualTo desiredTextMask
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Extend a PinnedThread`() {
        val desiredTextMask = textTestMask
        val desiredBinaryMask = binaryTestMask
        val desiredAffinityDescriptor = testAffinityDescriptor
        val runnable = unlatchAndSleepRunnable(true)
        val pinned = ExtendedPinnedThread(desiredAffinityDescriptor, runnable)
        try {
            pinned.start()
            latch.await()
            pinned.affinity().mask() shouldBeEqualTo desiredBinaryMask
            pinned.affinity().toString() shouldBeEqualTo desiredTextMask
        } finally {
            pinned.interrupt()
        }
    }

    @Test
    fun `Unsupported platform behavior - PinnedThread access`() {
        unsupportedPlatform {
            val desiredAffinityDescriptor = testAffinityDescriptor
            val runnable = unlatchAndSleepRunnable()
            val pinned = PinnedThread(runnable)
            try {
                pinned.start()
                latch.await()
                pinned.nativeId() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
                pinned.affinity() shouldBeEqualTo AffinityDescriptor.UNSUPPORTED
                pinned.affinity(desiredAffinityDescriptor)
                pinned.affinity() shouldBeEqualTo AffinityDescriptor.UNSUPPORTED
            } finally {
                pinned.interrupt()
            }
        }
    }

    private class ExtendedPinnedThread(
            affinity: AffinityDescriptor,
            private val runnable: Runnable) : PinnedThread(affinity) {
        override fun run() = runnable.run()
    }

    private fun unlatchAndSleepRunnable(setup: Boolean = false) = Runnable {
        if (setup) (Thread.currentThread() as? PinnedThread)?.initialize()
        latch.countDown()
        runCatching { Thread.sleep(1000L) }
    }
}
