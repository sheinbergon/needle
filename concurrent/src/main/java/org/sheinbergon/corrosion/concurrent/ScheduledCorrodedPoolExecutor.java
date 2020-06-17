package org.sheinbergon.corrosion.concurrent;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledCorrodedPoolExecutor extends ScheduledThreadPoolExecutor {

    public static ScheduledExecutorService newSingleCorrodedScheduledExecutor(
            final @Nonnull CorrodedFactory corrodedFactory) {
        return new ScheduledCorrodedPoolExecutor(1, corrodedFactory);
    }

    public static ExecutorService newScheduledCorrodedPool(final int size,
                                                           final @Nonnull CorrodedFactory corrodedFactory) {
        return new ScheduledCorrodedPoolExecutor(size, corrodedFactory);
    }

    public ScheduledCorrodedPoolExecutor(int poolSize,
                                         final @Nonnull CorrodedFactory corrodedFactory) {
        super(poolSize, corrodedFactory);
    }
}