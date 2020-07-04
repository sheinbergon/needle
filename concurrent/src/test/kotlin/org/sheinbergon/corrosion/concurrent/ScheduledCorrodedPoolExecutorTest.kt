package org.sheinbergon.corrosion.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ScheduledCorrodedPoolExecutorTest {

    @Test
    fun `Single corroded scheduled executor`() {
        val scheduler = ScheduledCorrodedPoolExecutor.newSingleCorrodedScheduledExecutor(TestMaskCorrodedFactory)
        testCorrodedExecutor(`1`, scheduler as ScheduledCorrodedPoolExecutor)
    }

    @Test
    fun `Pooled corroded scheduled executor`() {
        val scheduler = ScheduledCorrodedPoolExecutor.newScheduledCorrodedPool(availableCores, TestMaskCorrodedFactory)
        testCorrodedExecutor(availableCores, scheduler as ScheduledCorrodedPoolExecutor)
    }

    private fun testCorrodedExecutor(
            concurrency: Int,
            scheduler: ScheduledCorrodedPoolExecutor) = scheduler.use {
        val visited = Sets.newConcurrentHashSet<Corroded>()
        val latch = CountDownLatch(concurrency)
        scheduler.corePoolSize shouldBeEqualTo concurrency
        val futures = (`0` until concurrency)
                .map { runnableTask(latch, visited) }
                .map { scheduler.schedule(it, DELAY, TimeUnit.MILLISECONDS) }
        latch.await()
        futures.forEach { it.isDone shouldBeEqualTo true }
        visited.size shouldBeEqualTo concurrency
    }
}