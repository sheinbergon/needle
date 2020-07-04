package org.sheinbergon.needle.concurrent;

import com.google.common.collect.Sets;
import lombok.val;
import org.sheinbergon.needle.PinnedThread;
import org.sheinbergon.needle.concurrent.util.ResettableOneOffLatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class GovernedAffinityPinnedThreadFactory implements PinnedThreadFactory {


    @Nonnull
    private final Set<PinnedThread> instances = Sets.newHashSet();
    @Nonnull
    private final Lock instancesLock = new ReentrantLock();
    @Nonnull
    private final ResettableOneOffLatch startupLatch = new ResettableOneOffLatch();
    @Nullable
    private volatile Long binaryMask;
    @Nullable
    private volatile String textMask;

    /* No args constructor build pinned thread without an affinity mask
     * You can also set one after the thread has started it's execution
     */
    public GovernedAffinityPinnedThreadFactory() {
        this(null, null);
    }

    public GovernedAffinityPinnedThreadFactory(final @Nonnull Long mask) {
        this(null, mask);
    }

    public GovernedAffinityPinnedThreadFactory(final @Nonnull String mask) {
        this(mask, null);
    }

    private GovernedAffinityPinnedThreadFactory(final @Nullable String textMask,
                                                final @Nullable Long binaryMask) {
        this.textMask = textMask;
        this.binaryMask = binaryMask;
    }

    public int goverened() {
        startupLatch.await(false);
        val size = new AtomicInteger();
        safe(instances -> size.set(instances.size()));
        return size.get();
    }

    public void alter(
            final @Nonnull String textMask,
            final boolean affectRunning) {
        this.textMask = textMask;
        this.binaryMask = null;
        if (affectRunning) alter(pinned -> pinned.affinity(textMask));
    }

    public void alter(
            final long binaryMask,
            final boolean affectRunning) {
        this.textMask = null;
        this.binaryMask = binaryMask;
        if (affectRunning) alter(pinned -> pinned.affinity(binaryMask));
    }

    @Override
    public final PinnedThread newThread(final @Nonnull Runnable r) {
        startupLatch.await(true);
        val pinned = pinned(r);
        safe(instances -> instances.add(pinned));
        return pinned;
    }

    private void safe(final @Nonnull Consumer<Set<PinnedThread>> action) {
        instancesLock.lock();
        try {
            action.accept(instances);
        } finally {
            instancesLock.unlock();
        }
    }

    private PinnedThread pinned(final @Nonnull Runnable r) {
        if (textMask != null) {
            return new GovernedPinnedThread(r, textMask);
        } else if (binaryMask != null) {
            return new GovernedPinnedThread(r, binaryMask);
        } else {
            return new GovernedPinnedThread(r);
        }
    }

    private void alter(final @Nonnull Consumer<PinnedThread> fn) {
        startupLatch.await(false);
        safe(instances -> instances.forEach(fn));
    }

    private final class GovernedPinnedThread extends PinnedThread {

        GovernedPinnedThread(final @Nonnull Runnable target, final long mask) {
            super(target, mask);
        }

        GovernedPinnedThread(final @Nonnull Runnable target, @Nonnull final String mask) {
            super(target, mask);
        }

        GovernedPinnedThread(final @Nonnull Runnable target) {
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