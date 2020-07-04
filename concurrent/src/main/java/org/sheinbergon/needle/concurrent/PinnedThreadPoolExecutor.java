package org.sheinbergon.needle.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.*;

public class PinnedThreadPoolExecutor extends ThreadPoolExecutor implements Closeable {

    public PinnedThreadPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final @Nonnull TimeUnit unit,
            final @Nonnull BlockingQueue<Runnable> workQueue,
            final @Nonnull PinnedThreadFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
    }

    public static ExecutorService newSinglePinnedExecutor(final @Nonnull PinnedThreadFactory factory) {
        return new PinnedThreadPoolExecutor(NumberUtils.INTEGER_ONE,
                NumberUtils.INTEGER_ONE,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                factory);
    }

    public static ExecutorService newFixedPinnedPool(final int size, final @Nonnull PinnedThreadFactory factory) {
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