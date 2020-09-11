package org.sheinbergon.needle.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch

class PinnedThreadPoolExecutorTest {

    @Test
    fun `Single pinnned thread executor`() {
        val executor = PinnedThreadPoolExecutor.newSinglePinnedThreadExecutor(TestMaskPinnedThreadFactory)
        testPinnedThreadExecutor(`1`, executor as PinnedThreadPoolExecutor)
    }

    @Test
    fun `Fixed pinned thread pool executor`() {
        val executor = PinnedThreadPoolExecutor.newFixedPinnedThreadPool(availableCores, TestMaskPinnedThreadFactory)
        testPinnedThreadExecutor(availableCores, executor as PinnedThreadPoolExecutor)
    }

    private fun testPinnedThreadExecutor(concurrency: Int, pool: PinnedThreadPoolExecutor) = pool.use {
        val visited = Sets.newConcurrentHashSet<Pinned>()
        val latch = CountDownLatch(concurrency)
        pool.corePoolSize shouldBeEqualTo concurrency
        val tasks = (`0` until concurrency).map { callableTask(latch, visited) }
        val futures = pool.invokeAll(tasks)
        latch.await()
        Thread.sleep(5L)
        futures.forEach { it.isDone shouldBe true }
        visited.size shouldBeEqualTo concurrency
    }
}
