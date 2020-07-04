package org.sheinbergon.corrosion.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledCorrodedPoolExecutor extends ScheduledThreadPoolExecutor implements Closeable {

    public ScheduledCorrodedPoolExecutor(int size, final @Nonnull CorrodedFactory factory) {
        super(size, factory);
    }

    public static ScheduledExecutorService newSingleCorrodedScheduledExecutor(final @Nonnull CorrodedFactory factory) {
        return new ScheduledCorrodedPoolExecutor(NumberUtils.INTEGER_ONE, factory);
    }

    public static ExecutorService newScheduledCorrodedPool(final int size, final @Nonnull CorrodedFactory factory) {
        return new ScheduledCorrodedPoolExecutor(size, factory);
    }

    @Override
    public void close() {
        shutdown();
    }
}