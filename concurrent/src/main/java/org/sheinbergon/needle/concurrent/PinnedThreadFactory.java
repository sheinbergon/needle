package org.sheinbergon.needle.concurrent;

import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;

public interface PinnedThreadFactory extends ThreadFactory, ForkJoinPool.ForkJoinWorkerThreadFactory {

  /**
   * Constructs a new {@link PinnedThread}.
   *
   * @param r a runnable to be executed by new thread instance
   * @return instantiated {@link PinnedThread}, or {@code null} if the request to
   * create a thread is rejected
   */
  @Nullable
  PinnedThread newThread(@Nonnull Runnable r);

  /**
   * @param pool the pool this pinned worker thread operates in
   * @return the pinned worker thread, or null if the implementation rejects its inception
   */
  @Nullable
  PinnedThread.ForkJoinWorker newThread(@Nonnull ForkJoinPool pool);
}
