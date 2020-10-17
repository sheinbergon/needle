package org.sheinbergon.needle.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.`0`
import org.sheinbergon.needle.`1`
import org.sheinbergon.needle.`2`
import org.sheinbergon.needle.Pinned
import org.sheinbergon.needle.PinnedThread
import org.sheinbergon.needle.binaryTestMask
import org.sheinbergon.needle.concurrent.util.ResettableOneOffLatch
import org.sheinbergon.needle.default
import org.sheinbergon.needle.negatedBinaryTestMask
import org.sheinbergon.needle.negatedTestAffinityDescriptor
import org.sheinbergon.needle.testAffinityDescriptor
import java.util.concurrent.RecursiveAction

class GovernedAffinityPinnedThreadFactoryTest {

  private lateinit var latch: ResettableOneOffLatch

  private fun unlatchAndSleepTask() = Runnable {
    latch.fire()
    runCatching { Thread.sleep(1000L) }
  }

  private inner class UnlatchAndSleepAction : RecursiveAction() {

    lateinit var pinned: Pinned
      private set

    override fun compute() {
      pinned = Thread.currentThread() as PinnedThread.ForkJoinWorker
      unlatchAndSleepTask().run()
    }
  }

  @BeforeEach
  fun setup() {
    latch = ResettableOneOffLatch(true)
  }

  @Test
  fun `Initialize the factory without a mask and alter the affinity of a created pinned thread`() {
    val factory = GovernedAffinityPinnedThreadFactory()
    val pinned = factory.newThread(unlatchAndSleepTask())
    pinned!!.start()
    latch.await(true)
    val original = pinned.affinity()
    original.mask() shouldBeEqualTo default.mask()
    factory.alter(negatedTestAffinityDescriptor, true)
    val altered = pinned.affinity()
    altered.mask() shouldBeEqualTo negatedBinaryTestMask
  }

  @Test
  fun `Initialize the factory using a binary mask and alter the affinity of newly created pinned threads`() {
    val factory = GovernedAffinityPinnedThreadFactory(testAffinityDescriptor)
    val pinned1 = factory.newThread(unlatchAndSleepTask())
    pinned1!!.start()
    latch.await(true)
    val original = pinned1.affinity()
    original.mask() shouldBeEqualTo binaryTestMask
    factory.alter(negatedTestAffinityDescriptor, false)
    val unaltered = pinned1.affinity()
    unaltered.mask() shouldBeEqualTo binaryTestMask
    val pinned2 = factory.newThread(unlatchAndSleepTask())
    pinned2!!.start()
    latch.await(false)
    val altered = pinned2.affinity()
    altered.mask() shouldBeEqualTo negatedBinaryTestMask
  }

  @Test
  fun `Verify governed pinned threads factory behavior`() {
    val factory = GovernedAffinityPinnedThreadFactory(testAffinityDescriptor)
    factory.governed() shouldBeEqualTo `0`
    val pinned1 = factory.newThread(unlatchAndSleepTask())
    pinned1!!.start()
    factory.governed() shouldBeEqualTo `1`
    latch.await(true)
    val original = pinned1.affinity()
    original.mask() shouldBeEqualTo binaryTestMask
    factory.alter(negatedTestAffinityDescriptor, true)
    val altered = pinned1.affinity()
    altered.mask() shouldBeEqualTo negatedBinaryTestMask
    val pinned2 = factory.newThread(unlatchAndSleepTask())
    pinned2!!.start()
    factory.governed() shouldBeEqualTo `2`
    latch.await(false)
    Thread.sleep(2000L)
    factory.governed() shouldBeEqualTo `0`
  }

  @Test
  fun `Initialize the factory without a mask and alter the affinity of created pinned fork-join threads`() {
    val factory = GovernedAffinityPinnedThreadFactory()
    PinnedExecutors.newPinnedWorkStealingPool(`1`, factory).let { pool ->
      val action = UnlatchAndSleepAction()
      pool.execute(action)
      latch.await(true)
      val pinned = action.pinned
      val original = pinned.affinity()
      original.mask() shouldBeEqualTo default.mask()
      factory.alter(negatedTestAffinityDescriptor, true)
      val altered = pinned.affinity()
      altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }
  }

  @Test
  fun `Initialize the factory using a mask and alter the affinity of newly created pinned fork-join threads`() {
    val factory = GovernedAffinityPinnedThreadFactory(testAffinityDescriptor)
    PinnedExecutors.newPinnedWorkStealingPool(`2`, factory).let { pool ->
      val action1 = UnlatchAndSleepAction()
      pool.execute(action1)
      latch.await(true)
      val pinned1 = action1.pinned
      val original = pinned1.affinity()
      original.mask() shouldBeEqualTo binaryTestMask
      factory.alter(negatedTestAffinityDescriptor, false)
      val unaltered = pinned1.affinity()
      unaltered.mask() shouldBeEqualTo binaryTestMask
      val action2 = UnlatchAndSleepAction()
      pool.execute(action2)
      latch.await(false)
      val pinned2 = action2.pinned
      val altered = pinned2.affinity()
      altered.mask() shouldBeEqualTo negatedBinaryTestMask
    }
  }
}
