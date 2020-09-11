package org.sheinbergon.needle.concurrent;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.ForkJoinPool;

public final class PinnedForkJoinPool extends ForkJoinPool implements Closeable {

    /**
     * Creates a new affinity aware {@code PinnedForkJoinPool} with the given initial
     * parameters.
     *
     * @param parallelism the parallelism level (amount of worker threads to be spawned)
     * @param factory     the {@code PinnedThread} factory to use when the executor
     *                    creates new fork-join worker threads
     */
    public PinnedForkJoinPool(
            final int parallelism,
            final @Nonnull PinnedThreadFactory factory) {
        super(parallelism, factory, null, false);
    }

    @Override
    public void close() {
        shutdown();
    }
}
