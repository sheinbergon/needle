package org.sheinbergon.corrosion.concurrent;

import org.sheinbergon.corrosion.Corroded;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FixedAffinityCorrodedFactory implements CorrodedFactory {


    /* No args constructor build corroded thread without an affinity mask
     * You can also set one after the thread has started it's execution
     */
    public FixedAffinityCorrodedFactory() {
        this(null, null);
    }

    public FixedAffinityCorrodedFactory(final @Nonnull Long mask) {
        this(null, mask);
    }

    public FixedAffinityCorrodedFactory(final @Nonnull String mask) {
        this(mask, null);
    }

    private FixedAffinityCorrodedFactory(final @Nullable String textMask,
                                         final @Nullable Long binaryMask) {
        this.textMask = textMask;
        this.binaryMask = binaryMask;
    }

    @Nullable
    private final Long binaryMask;

    @Nullable
    private final String textMask;

    @Override
    public final Corroded newThread(final @Nonnull Runnable r) {
        if (textMask != null) {
            return new Corroded(r, textMask);
        } else if (binaryMask != null) {
            return new Corroded(r, binaryMask);
        } else {
            return new Corroded(r);
        }
    }
}