package org.sheinbergon.corrosion.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.*
import java.util.concurrent.CountDownLatch

class CorrodedPoolExecutorTest {

    @Test
    fun `Single corroded executor`() {
        val executor = CorrodedPoolExecutor.newSingleCorrodedExecutor(TestMaskCorrodedFactory)
        testCorrodedExecutor(`1`, executor as CorrodedPoolExecutor)
    }

    @Test
    fun `Fixed corroded pool executor`() {
        val executor = CorrodedPoolExecutor.newFixedCorrodedPool(availableCores, TestMaskCorrodedFactory)
        testCorrodedExecutor(availableCores, executor as CorrodedPoolExecutor)
    }

    private fun testCorrodedExecutor(
            concurrency: Int,
            pool: CorrodedPoolExecutor) = pool.use {
        val visited = Sets.newConcurrentHashSet<Corroded>()
        val latch = CountDownLatch(concurrency)
        pool.corePoolSize shouldBeEqualTo concurrency
        val tasks = (`0` until concurrency).map { callableTask(latch, visited) }
        val futures = pool.invokeAll(tasks)
        latch.await()
        futures.forEach { it.isDone shouldBe true }
        visited.size shouldBeEqualTo concurrency
    }
}