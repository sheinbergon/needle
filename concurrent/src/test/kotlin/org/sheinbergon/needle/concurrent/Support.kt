package org.sheinbergon.needle.concurrent

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.sheinbergon.needle.PinnedThread
import org.sheinbergon.needle.binaryTestMask
import org.sheinbergon.needle.testAffinityDescriptor
import org.sheinbergon.needle.textTestMask
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

internal const val SCHEDULING_DELAY = 500L

internal object TestMaskPinnedThreadFactory : PinnedThreadFactory {
    override fun newThread(r: Runnable) = PinnedThread(r, testAffinityDescriptor)

    override fun newThread(pool: ForkJoinPool) = PinnedThread.ForkJoinWorker(pool, testAffinityDescriptor)
}

@Suppress("UNCHECKED_CAST")
internal fun callableTask(latch: CountDownLatch, visited: MutableSet<PinnedThread>): Callable<Unit> =
    Executors.callable { runnableTask(latch, visited).run() } as Callable<Unit>

internal fun runnableTask(latch: CountDownLatch, visited: MutableSet<PinnedThread>) = Runnable {
    val self = Thread.currentThread() as PinnedThread
    visited.add(self) shouldBe true
    self.affinity().mask() shouldBeEqualTo binaryTestMask
    self.affinity().toString() shouldBeEqualTo textTestMask
    Thread.sleep(SCHEDULING_DELAY)
    latch.countDown()
}
