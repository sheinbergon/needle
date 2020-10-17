package org.sheinbergon.needle.concurrent

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RecursiveAction

class FixedAffinityPinnedThreadFactoryTest {

  @Test
  fun `Initialize the factory`() {
    val factory = FixedAffinityPinnedThreadFactory(testAffinityDescriptor)
    testPinnedThreadInception(factory)
    testPinnedForkJoinWorkerThreadInception(factory)
  }

  private fun testPinnedThreadInception(factory: PinnedThreadFactory) {
    val latch = CountDownLatch(`1`)
    val pinned = factory.newThread(task(latch))
    pinned?.start()
    latch.await()
  }

  private fun testPinnedForkJoinWorkerThreadInception(factory: PinnedThreadFactory) {
    val latch = CountDownLatch(`1`)
    PinnedExecutors.newPinnedWorkStealingPool(`1`, factory).let {
      val action = action(latch)
      it.submit(action)
      latch.await()
      Thread.sleep(5L)
      action.isDone.shouldBeTrue()
    }
  }

  private fun action(latch: CountDownLatch) = object : RecursiveAction() {
    override fun compute() = task(latch).run()
  }

  private fun task(latch: CountDownLatch) = Runnable {
    val self = Thread.currentThread() as Pinned
    self.affinity().mask() shouldBeEqualTo binaryTestMask
    self.affinity().toString() shouldBeEqualTo textTestMask
    latch.countDown()
  }
}
