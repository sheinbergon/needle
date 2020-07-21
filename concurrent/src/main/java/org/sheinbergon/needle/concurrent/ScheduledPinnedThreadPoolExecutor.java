package org.sheinbergon.needle.concurrent;

import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledPinnedThreadPoolExecutor extends ScheduledThreadPoolExecutor implements Closeable {

    public ScheduledPinnedThreadPoolExecutor(int size, final @Nonnull PinnedThreadFactory factory) {
        super(size, factory);
    }

    public static ScheduledExecutorService newSinglePinnedThreadScheduledExecutor(final @Nonnull PinnedThreadFactory factory) {
        return new ScheduledPinnedThreadPoolExecutor(NumberUtils.INTEGER_ONE, factory);
    }

    public static ExecutorService newScheduledPinnedThreadPool(final int size, final @Nonnull PinnedThreadFactory factory) {
        return new ScheduledPinnedThreadPoolExecutor(size, factory);
    }

    @Override
    public void close() {
        shutdown();
    }
}