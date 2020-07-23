package org.sheinbergon.needle.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class PinnedThreadPoolExecutor extends ThreadPoolExecutor implements Closeable {

    /**
     * Creates a new affinity aware {@code PinnedThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize    the number of {@code PinnedThread} instances to keep in the pool,
     *                        even if they are idle, unless {@code allowCoreThreadTimeOut}
     *                        is set
     * @param maximumPoolSize the maximum number of {@code PinnedThread} instances to allow in the
     *                        pool
     * @param keepAliveTime   when the number of threads is greater than
     *                        the core, this is the maximum time that excess idle threads
     *                        will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are
     *                        executed.  This queue will hold only the {@code Runnable}
     *                        tasks submitted by the {@code execute} method.
     * @param factory         the {@code PinnedThread} factory to use when the executor
     *                        creates a new thread
     */
    public PinnedThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final @Nonnull TimeUnit unit,
            final @Nonnull BlockingQueue<Runnable> workQueue,
            final @Nonnull PinnedThreadFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
    }

    /**
     * Static factory methods for affinity aware single-thread {@code ExecutorService} inception.
     *
     * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
     * @return the affinity aware {@code ExecutorService}
     */
    public static ExecutorService newSinglePinnedThreadExecutor(final @Nonnull PinnedThreadFactory factory) {
        return new PinnedThreadPoolExecutor(NumberUtils.INTEGER_ONE,
                NumberUtils.INTEGER_ONE,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                factory);
    }

    /**
     * Static factory methods for affinity aware fixed-size {@code ExecutorService} inception.
     *
     * @param size    number of {@code PinnedThread} instances to maintain in the pool
     * @param factory the {@code PinnedThreadFactory} used create affinity aware {@code PinnedThread} instances
     * @return the affinity aware {@code ExecutorService}
     */
    public static ExecutorService newFixedPinnedThreadPool(final int size, final @Nonnull PinnedThreadFactory factory) {
        return new PinnedThreadPoolExecutor(size,
                size,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                factory);
    }

    @Override
    public void close() {
        shutdown();
    }
}
