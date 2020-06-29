package org.sheinbergon.corrosion.concurrent

import com.google.common.collect.Sets
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.Corroded
import org.sheinbergon.corrosion.*
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class CorrodedPoolExecutorTest {

    @Test
    fun `Single corroded pool executor`() {
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
            pool: CorrodedPoolExecutor) = pool.use<CorrodedPoolExecutor, Unit> {
        val visited = Sets.newConcurrentHashSet<Corroded>()
        val latch = CountDownLatch(concurrency)
        pool.corePoolSize shouldBeEqualTo concurrency
        val tasks = (`0` until concurrency).map { task(latch, visited) }
        pool.invokeAll(tasks)
        latch.await()
        visited.size shouldBeEqualTo concurrency
    }

private fun task(
        latch: CountDownLatch,
        visited: MutableSet<Corroded>) = Callable<Unit> {
    val self = Thread.currentThread() as Corroded
    visited.add(self)
    self.affinity().mask() shouldBeEqualTo binaryTestMask
    self.affinity().toString() shouldBeEqualTo textTestMask
    latch.countDown()
}
}