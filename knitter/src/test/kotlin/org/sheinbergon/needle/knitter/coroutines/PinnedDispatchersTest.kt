package org.sheinbergon.needle.knitter.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.AffinityDescriptor
import org.sheinbergon.needle.Needle
import org.sheinbergon.needle.availableCores
import org.sheinbergon.needle.binaryTestMask
import org.sheinbergon.needle.negatedBinaryTestMask
import org.sheinbergon.needle.negatedTestAffinityDescriptor
import org.sheinbergon.needle.negatedTextTestMask
import org.sheinbergon.needle.testAffinityDescriptor
import org.sheinbergon.needle.textTestMask

class PinnedDispatchersTest {

  @Test
  fun `Fixed affinity single threaded dispatcher`() {
    val dispatcher = fixedAffinitySingleThread(testAffinityDescriptor)
    val deferred = deferredAffinitySingleAsync(dispatcher)
    runBlocking { blockingAssertSingle(deferred, binaryTestMask, textTestMask) }
  }

  @Test
  fun `Fixed affinity thread-pool dispatcher`() {
    val dispatcher = fixedAffinityThreadPool(availableCores, negatedTestAffinityDescriptor)
    val deferred = deferredAffinityPoolAsync(availableCores, dispatcher)
    runBlocking { blockingAssertPool(availableCores, deferred, negatedBinaryTestMask, negatedTextTestMask) }
  }

  @Test
  fun `Governed affinity single threaded dispatcher`() {
    val dispatcher = governedAffinitySingleThread(testAffinityDescriptor)
    val deferred1 = deferredAffinitySingleAsync(dispatcher)
    runBlocking { blockingAssertSingle(deferred1, binaryTestMask, textTestMask) }
    dispatcher.alter(negatedTestAffinityDescriptor)
    val deferred2 = deferredAffinitySingleAsync(dispatcher)
    runBlocking { blockingAssertSingle(deferred2, negatedBinaryTestMask, negatedTextTestMask) }
  }

  @Test
  fun `Governed affinity thread-pool dispatcher`() {
    val dispatcher = governedAffinityThreadPool(availableCores, negatedTestAffinityDescriptor)
    val deferred1 = deferredAffinityPoolAsync(availableCores, dispatcher)
    runBlocking { blockingAssertPool(availableCores, deferred1, negatedBinaryTestMask, negatedTextTestMask) }
    dispatcher.alter(testAffinityDescriptor)
    val deferred2 = deferredAffinityPoolAsync(availableCores, dispatcher)
    runBlocking { blockingAssertPool(availableCores, deferred2, binaryTestMask, textTestMask) }
  }

  private fun deferredAffinitySingleAsync(
    dispatcher: CoroutineDispatcher
  ) = CoroutineScope(dispatcher).async {
    Needle.affinity()
  }

  private fun deferredAffinityPoolAsync(
    cores: Int,
    dispatcher: CoroutineDispatcher
  ) = CoroutineScope(dispatcher).run {
    (`1`..cores).map {
      async {
        Thread.currentThread() to Needle.affinity()
      }
    }
  }

  private suspend fun blockingAssertSingle(
    deferred: Deferred<AffinityDescriptor>,
    binaryMask: Long,
    textMask: String
  ) {
    val affinity = deferred.await()
    affinity.mask() shouldBeEqualTo binaryMask
    affinity.toString() shouldBeEqualTo textMask
  }

  private suspend fun blockingAssertPool(
    cores: Int,
    deferred: List<Deferred<Pair<Thread, AffinityDescriptor>>>,
    binaryMask: Long,
    textMask: String
  ) {
    val results = deferred.awaitAll()
    val threads = results.mapTo(mutableSetOf(), Pair<Thread, *>::first)
    threads.size shouldBeLessOrEqualTo cores
    results.forEach { (_, affinity) ->
      affinity.mask() shouldBeEqualTo binaryMask
      affinity.toString() shouldBeEqualTo textMask
    }
  }
}
