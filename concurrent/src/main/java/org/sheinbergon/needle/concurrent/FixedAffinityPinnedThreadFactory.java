package org.sheinbergon.needle.concurrent;

import lombok.RequiredArgsConstructor;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public final class FixedAffinityPinnedThreadFactory implements PinnedThreadFactory {

    @Nonnull
    private final AffinityDescriptor affinity;

    @Override
    public final PinnedThread newThread(final @Nonnull Runnable r) {
        return new PinnedThread(r, affinity);
    }
}