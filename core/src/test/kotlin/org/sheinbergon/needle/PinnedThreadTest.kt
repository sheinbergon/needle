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
    fun `Access a corroded affinity information without starting it`() {
        val runnable = unlatchAndSleepRunnable()
        val corroded = PinnedThread(runnable)
        Assertions.assertThrows(NeedleException::class.java) { corroded.affinity() }
    }

    @Test
    fun `Start a Corroded without an affinity mask`() {
        val runnable = unlatchAndSleepRunnable()
        val corroded = PinnedThread(runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity() shouldBeEqualTo default
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Start a Corroded with a binary mask`() {
        val desired: Long = binaryTestMask
        val runnable = unlatchAndSleepRunnable()
        val corroded = PinnedThread(runnable, desired)
        try {
            corroded.start()
            latch.await()
            corroded.affinity().mask() shouldBeEqualTo desired
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Start a Corroded with a text mask`() {
        val desired = textTestMask
        val runnable = unlatchAndSleepRunnable()
        val corroded = PinnedThread(runnable, desired)
        try {
            corroded.start()
            latch.await()
            corroded.affinity().toString() shouldBeEqualTo desired
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Change the affinity of a Corroded using a text mask during runtime`() {
        val desiredTextMask = textTestMask
        val desiredBinaryMask = binaryTestMask
        val runnable = unlatchAndSleepRunnable()
        val corroded = PinnedThread(runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity() shouldBeEqualTo default
            corroded.affinity(desiredTextMask)
            corroded.affinity() shouldNotBeEqualTo default
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredTextMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Extend a corroded using a text mask`() {
        val desiredTextMask = textTestMask
        val desiredBinaryMask = binaryTestMask
        val runnable = unlatchAndSleepRunnable(true)
        val corroded = ExtendedPinnedThreadTextMask(desiredTextMask, runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredTextMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Extend a corroded using binary mask`() {
        val desiredTextMask = textTestMask
        val desiredBinaryMask: Long = binaryTestMask
        val runnable = unlatchAndSleepRunnable(true)
        val corroded = ExtendedPinnedThreadBinaryMask(desiredBinaryMask, runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredTextMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Unsupported platform behavior - Corroded access`() {
        unsupportedPlatform {
            val desiredTextMask = textTestMask
            val runnable = unlatchAndSleepRunnable()
            val corroded = PinnedThread(runnable)
            try {
                corroded.start()
                latch.await()
                corroded.nativeId() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
                corroded.affinity() shouldBeEqualTo AffinityDescriptor.UNSUPPORTED
                corroded.affinity(desiredTextMask)
                corroded.affinity() shouldBeEqualTo AffinityDescriptor.UNSUPPORTED
            } finally {
                corroded.interrupt()
            }
        }
    }

    private class ExtendedPinnedThreadTextMask(mask: String, private val runnable: Runnable) : PinnedThread(mask) {
        override fun run() = runnable.run()
    }

    private class ExtendedPinnedThreadBinaryMask(val mask: Long, private val runnable: Runnable) : PinnedThread(mask) {
        override fun run() = runnable.run()
    }

    private fun unlatchAndSleepRunnable(setup: Boolean = false) = Runnable {
        if (setup) (Thread.currentThread() as? PinnedThread)?.initialize()
        latch.countDown()
        runCatching { Thread.sleep(1000L) }
    }
}