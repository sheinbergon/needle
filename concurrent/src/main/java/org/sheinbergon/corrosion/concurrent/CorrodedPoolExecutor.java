package org.sheinbergon.corrosion.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.*;

public class CorrodedPoolExecutor extends ThreadPoolExecutor implements Closeable {

    public static ExecutorService newSingleCorrodedExecutor(final @Nonnull CorrodedFactory factory) {
        return new CorrodedPoolExecutor(NumberUtils.INTEGER_ONE,
                NumberUtils.INTEGER_ONE,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                factory);
    }

    public static ExecutorService newFixedCorrodedPool(final int size, final @Nonnull CorrodedFactory factory) {
        return new CorrodedPoolExecutor(size,
                size,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                factory);
    }

    public CorrodedPoolExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final @Nonnull TimeUnit unit,
            final @Nonnull BlockingQueue<Runnable> workQueue,
            final @Nonnull CorrodedFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
    }

    @Override
    public void close() {
        shutdown();
    }
}