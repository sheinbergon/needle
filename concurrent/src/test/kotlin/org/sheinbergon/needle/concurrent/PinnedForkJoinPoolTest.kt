package org.sheinbergon.needle.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch

class PinnedForkJoinPoolTest {

  @Test
  fun `Fixed affinity PinnedForkJoinPool behavior`() {
    val pool = PinnedForkJoinPool(availableCores, TestMaskPinnedThreadFactory)
    pool.use {
      testPinnedThreadExecutor(availableCores, pool)
    }
  }

  private fun testPinnedThreadExecutor(concurrency: Int, pool: PinnedForkJoinPool) = pool.use {
    val visited = Sets.newConcurrentHashSet<Pinned>()
    val latch = CountDownLatch(concurrency)
    pool.parallelism shouldBeEqualTo concurrency
    val actions = (`0` until concurrency).map { recursiveAction(latch, visited) }
    val tasks = actions.map { pool.submit(it) }
    latch.await()
    Thread.sleep(5L)
    tasks.forEach { it.isDone shouldBe true }
    visited.size shouldBeEqualTo concurrency
  }
}
