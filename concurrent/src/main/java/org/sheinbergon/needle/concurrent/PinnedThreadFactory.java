package org.sheinbergon.needle.concurrent;

import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public interface PinnedThreadFactory extends ThreadFactory {

    /**
     * Constructs a new {@link PinnedThread}.
     *
     * @param r a runnable to be executed by new thread instance
     * @return instantiated {@link PinnedThread}, or {@code null} if the request to
     * create a thread is rejected
     */
    PinnedThread newThread(@Nonnull Runnable r);
}
