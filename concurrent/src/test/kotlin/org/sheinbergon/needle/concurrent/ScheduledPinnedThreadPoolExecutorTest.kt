package org.sheinbergon.needle.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ScheduledPinnedThreadPoolExecutorTest {

    @Test
    fun `Single pinned thread scheduled executor`() {
        val scheduler = ScheduledPinnedThreadPoolExecutor.newSinglePinnedScheduledExecutor(TestMaskPinnedThreadFactory)
        testPinnedThreadExecutor(`1`, scheduler as ScheduledPinnedThreadPoolExecutor)
    }

    @Test
    fun `Pooled pinned thread scheduled executor`() {
        val scheduler = ScheduledPinnedThreadPoolExecutor.newScheduledPinnedPool(availableCores, TestMaskPinnedThreadFactory)
        testPinnedThreadExecutor(availableCores, scheduler as ScheduledPinnedThreadPoolExecutor)
    }

    private fun testPinnedThreadExecutor(
            concurrency: Int,
            scheduler: ScheduledPinnedThreadPoolExecutor
    ) = scheduler.use {
        val visited = Sets.newConcurrentHashSet<PinnedThread>()
        val latch = CountDownLatch(concurrency)
        scheduler.corePoolSize shouldBeEqualTo concurrency
        val futures = (`0` until concurrency)
                .map { runnableTask(latch, visited) }
                .map { scheduler.schedule(it, SCHEDULING_DELAY, TimeUnit.MILLISECONDS) }
        latch.await()
        Thread.sleep(5L)
        visited.size shouldBeEqualTo concurrency
        futures.forEach { it.isDone shouldBeEqualTo true }
    }
}