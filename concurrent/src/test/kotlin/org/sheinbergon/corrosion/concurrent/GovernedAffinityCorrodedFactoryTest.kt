package org.sheinbergon.corrosion.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.*
import org.sheinbergon.corrosion.concurrent.util.ResettableOneOffLatch

class GovernedAffinityCorrodedFactoryTest {

    private lateinit var latch: ResettableOneOffLatch

    private fun unlatchAndSleepTask() = Runnable {
        latch.fire()
        runCatching { Thread.sleep(1000L) }
    }

    @BeforeEach
    fun setup() {
        latch = ResettableOneOffLatch(true)
    }

    @Test
    fun `Initialize the factory without a mask and alter the affinity of a created corroded`() {
        val factory = GovernedAffinityCorrodedFactory()
        val corroded = factory.newThread(unlatchAndSleepTask())
        corroded.start()
        latch.await(true)
        val original = corroded.affinity()
        original.mask() shouldBeEqualTo default.mask()
        factory.alter(negatedBinaryTestMask, true)
        val altered = corroded.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }

    @Test
    fun `Initialize the factory using a binary a mask and alter the affinity of newly created corroded instances`() {
        val factory = GovernedAffinityCorrodedFactory(binaryTestMask)
        val corroded1 = factory.newThread(unlatchAndSleepTask())
        corroded1.start()
        latch.await(true)
        val original = corroded1.affinity()
        original.mask() shouldBeEqualTo binaryTestMask
        factory.alter(negatedBinaryTestMask, false)
        val unaltered = corroded1.affinity()
        unaltered.mask() shouldBeEqualTo binaryTestMask
        val corroded2 = factory.newThread(unlatchAndSleepTask())
        corroded2.start()
        latch.await(false)
        val altered = corroded2.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }

    @Test
    fun `Verify governed corroded instances factory behavior`() {
        val factory = GovernedAffinityCorrodedFactory(textTestMask)
        factory.goverened() shouldBeEqualTo `0`
        val corroded1 = factory.newThread(unlatchAndSleepTask())
        corroded1.start()
        factory.goverened() shouldBeEqualTo `1`
        latch.await(true)
        val original = corroded1.affinity()
        original.mask() shouldBeEqualTo binaryTestMask
        factory.alter(negatedTextTestMask, true)
        val altered = corroded1.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
        val corroded2 = factory.newThread(unlatchAndSleepTask())
        corroded2.start()
        factory.goverened() shouldBeEqualTo `2`
        latch.await(false)
        Thread.sleep(2000L)
        factory.goverened() shouldBeEqualTo `0`
    }

}