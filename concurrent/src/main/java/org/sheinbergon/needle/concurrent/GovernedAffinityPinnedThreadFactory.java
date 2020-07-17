package org.sheinbergon.needle.concurrent;

import com.google.common.collect.Sets;
import lombok.val;
import org.sheinbergon.needle.AffinityDescriptor;
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
    private volatile AffinityDescriptor affinity;

    /* No args constructor build pinned thread without an affinity mask
     * You can also set one after the thread has started it's execution
     */
    public GovernedAffinityPinnedThreadFactory() {
    }

    public GovernedAffinityPinnedThreadFactory(final @Nonnull AffinityDescriptor affinity) {
        this.affinity = affinity;
    }

    public int goverened() {
        startupLatch.await(false);
        val size = new AtomicInteger();
        safe(instances -> size.set(instances.size()));
        return size.get();
    }

    public void alter(
            final @Nonnull AffinityDescriptor affinity,
            final boolean affectRunning) {
        this.affinity = affinity;
        if (affectRunning) alter(pinned -> pinned.affinity(affinity));
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
        if (affinity != null) {
            return new GovernedPinnedThread(r, affinity);
        } else {
            return new GovernedPinnedThread(r);
        }
    }

    private void alter(final @Nonnull Consumer<PinnedThread> fn) {
        startupLatch.await(false);
        safe(instances -> instances.forEach(fn));
    }

    private final class GovernedPinnedThread extends PinnedThread {

        GovernedPinnedThread(final @Nonnull Runnable target, @Nonnull final AffinityDescriptor affinity) {
            super(target, affinity);
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