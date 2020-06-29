package org.sheinbergon.corrosion.concurrent

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.sheinbergon.corrosion.Corroded
import org.sheinbergon.corrosion.binaryTestMask
import org.sheinbergon.corrosion.textTestMask
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


internal const val DELAY = 500L

internal object TestMaskCorrodedFactory : CorrodedFactory {
    override fun newThread(r: Runnable) = Corroded(r, binaryTestMask)
}

@Suppress("UNCHECKED_CAST")
internal fun callableTask(latch: CountDownLatch, visited: MutableSet<Corroded>): Callable<Unit> =
        Executors.callable { runnableTask(latch, visited).run() } as Callable<Unit>


internal fun runnableTask(latch: CountDownLatch, visited: MutableSet<Corroded>) = Runnable {
    val self = Thread.currentThread() as Corroded
    visited.add(self) shouldBe true
    self.affinity().mask() shouldBeEqualTo binaryTestMask
    self.affinity().toString() shouldBeEqualTo textTestMask
    Thread.sleep(DELAY)
    latch.countDown()
}