package org.sheinbergon.needle.concurrent;

import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FixedAffinityPinnedThreadFactory implements PinnedThreadFactory {

    @Nullable
    private final Long binaryMask;
    @Nullable
    private final String textMask;

    public FixedAffinityPinnedThreadFactory(final @Nonnull Long mask) {
        this(null, mask);
    }

    public FixedAffinityPinnedThreadFactory(final @Nonnull String mask) {
        this(mask, null);
    }

    private FixedAffinityPinnedThreadFactory(final @Nullable String textMask,
                                             final @Nullable Long binaryMask) {
        this.textMask = textMask;
        this.binaryMask = binaryMask;
    }

    @Override
    public final PinnedThread newThread(final @Nonnull Runnable r) {
        if (textMask != null) {
            return new PinnedThread(r, textMask);
        } else if (binaryMask != null) {
            return new PinnedThread(r, binaryMask);
        } else {
            return new PinnedThread(r);
        }
    }
}