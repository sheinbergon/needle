package org.sheinbergon.needle.concurrent;

import lombok.RequiredArgsConstructor;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public final class FixedAffinityPinnedThreadFactory implements PinnedThreadFactory {

    /**
     * The {@link AffinityDescriptor} to use for setting affinity for
     * {@link PinnedThread} instances governed by this factory.
     */
    @Nonnull
    private final AffinityDescriptor affinity;

    @Override
    public PinnedThread newThread(final @Nonnull Runnable r) {
        return new PinnedThread(r, affinity);
    }
}
