package org.sheinbergon.needle.concurrent;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

public final class PinnedExecutors {

  /**
   * Static factory methods for affinity aware single-thread {@code ExecutorService} inception.
   *
   * @param factory the {@code PinnedThreadFactory} used to create affinity aware {@code PinnedThread} instances
   * @return the affinity aware {@code ExecutorService}
   */
  public static ExecutorService newSinglePinnedThreadExecutor(final @Nonnull PinnedThreadFactory factory) {
    return Executors.newSingleThreadExecutor(factory);
  }

  /**
   * Static factory methods for affinity aware fixed-size {@code ExecutorService} inception.
   *
   * @param size    number of {@code PinnedThread} instances to maintain in the pool
   * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
   * @return the affinity aware {@code ExecutorService}
   */
  public static ExecutorService newFixedPinnedThreadPool(final int size, final @Nonnull PinnedThreadFactory factory) {
    return Executors.newFixedThreadPool(size, factory);
  }

  /**
   * Static factory methods for affinity aware single-thread {@code ScheduledExecutorService} inception.
   *
   * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
   * @return the affinity aware {@code ScheduledExecutorService}
   */
  public static ScheduledExecutorService newSinglePinnedThreadScheduledExecutor(
      final @Nonnull PinnedThreadFactory factory) {
    return Executors.newSingleThreadScheduledExecutor(factory);
  }

  /**
   * Static factory methods for affinity aware fixed-size {@code ScheduledExecutorService} inception.
   *
   * @param size    number of {@code PinnedThread} instances to maintain in the pool
   * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
   * @return the affinity aware {@code ScheduledExecutorService}
   */
  public static ScheduledExecutorService newScheduledPinnedThreadPool(
      final int size,
      final @Nonnull PinnedThreadFactory factory) {
    return Executors.newScheduledThreadPool(size, factory);
  }

  /**
   * Creates a new affinity aware {@code ForkJoinPool} with the given parameters.
   *
   * @param parallelism the parallelism level (amount of worker threads to be spawned)
   * @param factory     the {@code PinnedThread} factory to use when the executor
   *                    creates new fork-join worker threads
   * @return an affinity aware {@code ForkJoinPool}
   */
  public static ForkJoinPool newPinnedWorkStealingPool(
      final int parallelism,
      final @Nonnull PinnedThreadFactory factory) {
    return new ForkJoinPool(parallelism, factory, null, true);
  }

  private PinnedExecutors() {
  }
}
