package org.sheinbergon.needle.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import org.sheinbergon.needle.concurrent.util.ResettableOneOffLatch

class GovernedAffinityPinnedThreadFactoryTest {

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
    fun `Initialize the factory without a mask and alter the affinity of a created pinned thread`() {
        val factory = GovernedAffinityPinnedThreadFactory()
        val pinned = factory.newThread(unlatchAndSleepTask())
        pinned.start()
        latch.await(true)
        val original = pinned.affinity()
        original.mask() shouldBeEqualTo default.mask()
        factory.alter(negatedTestAffinityDescriptor, true)
        val altered = pinned.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }

    @Test
    fun `Initialize the factory using a binary a mask and alter the affinity of newly created pinned threads`() {
        val factory = GovernedAffinityPinnedThreadFactory(testAffinityDescriptor)
        val pinned1 = factory.newThread(unlatchAndSleepTask())
        pinned1.start()
        latch.await(true)
        val original = pinned1.affinity()
        original.mask() shouldBeEqualTo binaryTestMask
        factory.alter(negatedTestAffinityDescriptor, false)
        val unaltered = pinned1.affinity()
        unaltered.mask() shouldBeEqualTo binaryTestMask
        val pinned2 = factory.newThread(unlatchAndSleepTask())
        pinned2.start()
        latch.await(false)
        val altered = pinned2.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }

    @Test
    fun `Verify governed pinned threads factory behavior`() {
        val factory = GovernedAffinityPinnedThreadFactory(testAffinityDescriptor)
        factory.governed() shouldBeEqualTo `0`
        val pinned1 = factory.newThread(unlatchAndSleepTask())
        pinned1.start()
        factory.governed() shouldBeEqualTo `1`
        latch.await(true)
        val original = pinned1.affinity()
        original.mask() shouldBeEqualTo binaryTestMask
        factory.alter(negatedTestAffinityDescriptor, true)
        val altered = pinned1.affinity()
        altered.mask() shouldBeEqualTo negatedBinaryTestMask
        val pinned2 = factory.newThread(unlatchAndSleepTask())
        pinned2.start()
        factory.governed() shouldBeEqualTo `2`
        latch.await(false)
        Thread.sleep(2000L)
        factory.governed() shouldBeEqualTo `0`
    }
}
