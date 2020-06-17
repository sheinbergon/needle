package org.sheinbergon.corrosion.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

public class CorrodedPoolExecutor extends ThreadPoolExecutor {

    public static ExecutorService newSingleCorrodedExecutor(final @Nonnull CorrodedFactory corrodedFactory) {
        return new CorrodedPoolExecutor(NumberUtils.INTEGER_ONE,
                NumberUtils.INTEGER_ONE,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                corrodedFactory);
    }

    public static ExecutorService newFixedCorrodedPool(final int size,
                                                       final @Nonnull CorrodedFactory corrodedFactory) {
        return new CorrodedPoolExecutor(size,
                size,
                NumberUtils.LONG_ZERO,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                corrodedFactory);
    }

    public CorrodedPoolExecutor(final int corePoolSize,
                                final int maximumPoolSize,
                                final long keepAliveTime,
                                final @Nonnull TimeUnit unit,
                                final @Nonnull BlockingQueue<Runnable> workQueue,
                                final @Nonnull CorrodedFactory corrodedFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, corrodedFactory);
    }

    public CorrodedPoolExecutor(final int corePoolSize,
                                final int maximumPoolSize,
                                final long keepAliveTime,
                                final @Nonnull TimeUnit unit,
                                final @Nonnull BlockingQueue<Runnable> workQueue,
                                final @Nonnull CorrodedFactory corrodedFactory,
                                final @Nonnull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, corrodedFactory, handler);
    }
}