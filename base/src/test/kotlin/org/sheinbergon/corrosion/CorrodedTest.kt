package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class CorrodedTest {

    @Test
    fun `Start a Corroded without an affinity mask`() {
        val default = Corrosion.get()
        val latch = CountDownLatch(1)
        val runnable = unlatchAndSleepRunnable(latch)
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
    fun `Start a Corroded with a binary affinity mask`() {
        val desired: Long = 0x0000000000000003
        val latch = CountDownLatch(1)
        val runnable = unlatchAndSleepRunnable(latch)
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
    fun `Start a Corroded with a string affinity mask`() {
        val desired = "3"
        val latch = CountDownLatch(1)
        val runnable = unlatchAndSleepRunnable(latch)
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
    fun `Change the affinity of a Corroded using a string mask during runtime`() {
        val desiredStringMask = "3"
        val desiredBinaryMask = 8L
        val default = Corrosion.get()
        val latch = CountDownLatch(1)
        val runnable = unlatchAndSleepRunnable(latch)
        val corroded = Corroded(runnable)
        try {
            corroded.start()
            latch.await()
            corroded.affinity() shouldBeEqualTo default
            corroded.affinity(desiredStringMask)
            corroded.affinity() shouldNotBeEqualTo default
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredStringMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Extend a corroded using a string mask`() {
        val desiredStringMask = "3"
        val desiredBinaryMask = 8L
        val latch = CountDownLatch(1)
        val corroded = object : Corroded(desiredStringMask) {
            override fun run() {
                setup()
                unlatchAndSleepRunnable(latch).run()
            }
        }
        try {
            corroded.start()
            latch.await()
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredStringMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Extend a corroded using binary mask`() {
        val desiredStringMask = "0-1"
        val desiredBinaryMask: Long = 0x0000000000000003
        val latch = CountDownLatch(1)
        val corroded = object : Corroded(desiredBinaryMask) {
            override fun run() {
                setup()
                unlatchAndSleepRunnable(latch).run()
            }
        }
        try {
            corroded.start()
            latch.await()
            corroded.affinity().mask() shouldBeEqualTo desiredBinaryMask
            corroded.affinity().toString() shouldBeEqualTo desiredStringMask
        } finally {
            corroded.interrupt()
        }
    }

    @Test
    fun `Unsupported platform behavior - Corroded access`() {
        unsupportedPlatform {
            val desiredStringMask = "3"
            val latch = CountDownLatch(1)
            val runnable = unlatchAndSleepRunnable(latch)
            val corroded = Corroded(runnable)
            try {
                corroded.start()
                latch.await()
                corroded.nativeId() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
                corroded.affinity() shouldBeEqualTo CoreSet.EMPTY
                corroded.affinity(desiredStringMask)
                corroded.affinity() shouldBeEqualTo CoreSet.EMPTY
            } finally {
                corroded.interrupt()
            }
        }
    }

    private fun unlatchAndSleepRunnable(latch: CountDownLatch) = Runnable {
        latch.countDown()
        runCatching { Thread.sleep(1000L) }
    }
}