package org.sheinbergon.corrosion.concurrent;

import com.google.common.collect.Sets;
import lombok.val;
import org.sheinbergon.corrosion.Corroded;
import org.sheinbergon.corrosion.concurrent.util.ResettableOneOffLatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class GovernedAffinityCorrodedFactory implements CorrodedFactory {


    /* No args constructor build corroded thread without an affinity mask
     * You can also set one after the thread has started it's execution
     */
    public GovernedAffinityCorrodedFactory() {
        this(null, null);
    }

    public GovernedAffinityCorrodedFactory(final @Nonnull Long mask) {
        this(null, mask);
    }

    public GovernedAffinityCorrodedFactory(final @Nonnull String mask) {
        this(mask, null);
    }

    private GovernedAffinityCorrodedFactory(final @Nullable String textMask,
                                            final @Nullable Long binaryMask) {
        this.textMask = textMask;
        this.binaryMask = binaryMask;
    }

    @Nullable
    private volatile Long binaryMask;

    @Nullable
    private volatile String textMask;

    @Nonnull
    private final Set<Corroded> instances = Sets.newHashSet();

    @Nonnull
    private final Lock instancesLock = new ReentrantLock();

    @Nonnull
    private final ResettableOneOffLatch startupLatch = new ResettableOneOffLatch();

    public void alter(
            final @Nonnull String textMask,
            final boolean affectRunning) {
        this.textMask = textMask;
        this.binaryMask = null;
        if (affectRunning) alter(corroded -> corroded.affinity(textMask));
    }

    public void alter(
            final long binaryMask,
            final boolean affectRunning) {
        this.textMask = null;
        this.binaryMask = binaryMask;
        if (affectRunning) alter(corroded -> corroded.affinity(binaryMask));
    }

    @Override
    public final Corroded newThread(final @Nonnull Runnable r) {
        startupLatch.await(true);
        val corroded = corroded(r);
        safe(instances -> instances.add(corroded));
        return corroded;
    }

    private void safe(final @Nonnull Consumer<Set<Corroded>> action) {
        instancesLock.lock();
        try {
            action.accept(instances);
        } finally {
            instancesLock.unlock();
        }
    }

    private Corroded corroded(final @Nonnull Runnable r) {
        if (textMask != null) {
            return new GovernedCorroded(r, textMask);
        } else if (binaryMask != null) {
            return new GovernedCorroded(r, binaryMask);
        } else {
            return new GovernedCorroded(r);
        }
    }

    private void alter(final @Nonnull Consumer<Corroded> fn) {
        startupLatch.await(false);
        safe(instances -> instances.forEach(fn));
    }

    private final class GovernedCorroded extends Corroded {

        GovernedCorroded(final @Nonnull Runnable target, final long mask) {
            super(target, mask);
        }

        GovernedCorroded(final @Nonnull Runnable target, @Nonnull final String mask) {
            super(target, mask);
        }

        GovernedCorroded(final @Nonnull Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            try {
                startupLatch.fire();
                super.run();
            } finally {
                safe(instances -> instances.remove(this));
            }
        }
    }
}