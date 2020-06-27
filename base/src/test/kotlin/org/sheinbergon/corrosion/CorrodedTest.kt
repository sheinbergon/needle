package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class CorrodedTest {

    private val default = Corrosion.get()
    private lateinit var latch: CountDownLatch

    @BeforeEach
    fun setup() {
        latch = CountDownLatch(1)
    }

    @Test
    fun `Start a Corroded without an affinity mask`() {
        val runnable = unlatchAndSleepRunnable()
        val corroded = Corroded(runnable)
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
        val corroded = Corroded(runnable, desired)
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
        val corroded = Corroded(runnable, desired)
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
        val corroded = Corroded(runnable)
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
        val corroded = ExtendedCorrodedTextMask(desiredTextMask, runnable)
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
        val corroded = ExtendedCorrodedBinaryMask(desiredBinaryMask, runnable)
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
            val corroded = Corroded(runnable)
            try {
                corroded.start()
                latch.await()
                corroded.nativeId() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
                corroded.affinity() shouldBeEqualTo CoreSet.EMPTY
                corroded.affinity(desiredTextMask)
                corroded.affinity() shouldBeEqualTo CoreSet.EMPTY
            } finally {
                corroded.interrupt()
            }
        }
    }

    private class ExtendedCorrodedTextMask(mask: String, private val runnable: Runnable) : Corroded(mask) {
        override fun run() = runnable.run()
    }

    private class ExtendedCorrodedBinaryMask(val mask: Long, private val runnable: Runnable) : Corroded(mask) {
        override fun run() = runnable.run()
    }

    private fun unlatchAndSleepRunnable(setup: Boolean = false) = Runnable {
        if (setup) (Thread.currentThread() as? Corroded)?.setup()
        latch.countDown()
        runCatching { Thread.sleep(1000L) }
    }
}