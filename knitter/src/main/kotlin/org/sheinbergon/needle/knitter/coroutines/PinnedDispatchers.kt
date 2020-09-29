package org.sheinbergon.needle.knitter.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.sheinbergon.needle.AffinityDescriptor
import org.sheinbergon.needle.concurrent.FixedAffinityPinnedThreadFactory
import org.sheinbergon.needle.concurrent.GovernedAffinityPinnedThreadFactory
import org.sheinbergon.needle.concurrent.PinnedThreadPoolExecutor
import kotlin.coroutines.CoroutineContext

private const val `1` = 1

private class GovernedAffinityDelegatingDispatcher(
    parallelism: Int,
    affinity: AffinityDescriptor
) : GovernedAffinityDispatcher() {

  val factory: GovernedAffinityPinnedThreadFactory = GovernedAffinityPinnedThreadFactory(affinity)

  private val delegate: CoroutineDispatcher

  init {
    val executor = PinnedThreadPoolExecutor.newFixedPinnedThreadPool(parallelism, factory)
    delegate = executor.asCoroutineDispatcher()
  }

  override fun alter(affinity: AffinityDescriptor) = factory.alter(affinity, true)

  override fun dispatch(context: CoroutineContext, block: Runnable) = delegate.dispatch(context, block)
}

fun governedAffinitySingleThread(affinity: AffinityDescriptor): GovernedAffinityDispatcher =
    governedAffinityThreadPool(`1`, affinity)

fun governedAffinityThreadPool(parallelism: Int, affinity: AffinityDescriptor): GovernedAffinityDispatcher =
    GovernedAffinityDelegatingDispatcher(parallelism, affinity)

fun fixedAffinitySingleThread(affinity: AffinityDescriptor) =
    fixedAffinityThreadPool(`1`, affinity)

fun fixedAffinityThreadPool(parallelism: Int, affinity: AffinityDescriptor): CoroutineDispatcher {
  val factory = FixedAffinityPinnedThreadFactory(affinity)
  val executor = PinnedThreadPoolExecutor.newFixedPinnedThreadPool(parallelism, factory)
  return executor.asCoroutineDispatcher()
}
