package org.sheinbergon.needle.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class ScheduledPinnedThreadPoolExecutor extends ScheduledThreadPoolExecutor implements Closeable {


    /**
     * Creates a new {@code ScheduledPinnedThreadPoolExecutor} with the
     * given initial parameters.
     *
     * @param size    the number of {@code PinnedThread} instances to keep in the pool
     * @param factory the factory to use when the executor creates a new thread
     */
    public ScheduledPinnedThreadPoolExecutor(
            final int size,
            final @Nonnull PinnedThreadFactory factory) {
        super(size, factory);
    }

    /**
     * Static factory methods for affinity aware single-thread {@code ScheduledExecutorService} inception.
     *
     * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
     * @return the affinity aware {@code ScheduledExecutorService}
     */
    public static ScheduledExecutorService newSinglePinnedThreadScheduledExecutor(
            final @Nonnull PinnedThreadFactory factory) {
        return new ScheduledPinnedThreadPoolExecutor(NumberUtils.INTEGER_ONE, factory);
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
        return new ScheduledPinnedThreadPoolExecutor(size, factory);
    }

    @Override
    public void close() {
        shutdown();
    }
}
