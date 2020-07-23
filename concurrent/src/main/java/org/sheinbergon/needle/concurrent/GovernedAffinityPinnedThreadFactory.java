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

    /**
     * A collection storing the live {@code PinnedThread} instances managed by this factory.
     */
    @Nonnull
    private final Set<PinnedThread> governed = Sets.newHashSet();

    /**
     * A lock used to synchronize accesses to the {@code GovernedAffinityPinnedThreadFactory#governed} collection.
     */
    @Nonnull
    private final Lock accessLock = new ReentrantLock();

    /**
     * A resettable latch used to ensure a predictable/thread-safe behavior for
     * the time frame between a {@code PinnedThread} inception to its start.
     */
    @Nonnull
    private final ResettableOneOffLatch pinnedThreadStartLatch = new ResettableOneOffLatch();

    /**
     * The mutable {@code AffinityDescriptor} to use for setting affinity for new (and existing)
     * {@code PinnedThread} instances governed by this factory.
     */
    @Nullable
    private volatile AffinityDescriptor affinity;

    /**
     * Build {@code PinnedThread} instances using the default process affinity
     * for newly created {@code PinnedThread} instances.
     * <p>
     * Note: you can set the affinity settings for both governed and future threads by calling
     * {@code GovernedAffinityPinnedThreadFactory#alter(Consumer)} beyond the instantiation of this factory
     */
    public GovernedAffinityPinnedThreadFactory() {
    }

    /**
     * Build {@code PinnedThread} instances using the given {@code AffinityDescriptor}
     * for newly created {@code PinnedThread} instances.
     * <p>
     * Note: you can set the affinity settings for both governed and future threads by calling
     * {@code GovernedAffinityPinnedThreadFactory#alter(Consumer)} beyond the instantiation of this factory
     * <p>
     *
     * @param affinityDescriptor The affinity descriptor to use for creating new {@code PinnedThread} instances
     */
    public GovernedAffinityPinnedThreadFactory(final @Nonnull AffinityDescriptor affinityDescriptor) {
        this.affinity = affinityDescriptor;
    }

    /**
     * Safely returns the live {@code PinnedThread } count governed by this factory.
     *
     * @return the amount of {@code PinnedThread} instances currently governed by this factory
     */
    public int governed() {
        pinnedThreadStartLatch.await(false);
        val size = new AtomicInteger();
        safe(() -> size.set(governed.size()));
        return size.get();
    }


    /**
     * Safely returns the live {@code PinnedThread } count governed by this factory.
     *
     * @param affinityDescriptor The affinity descriptor to use for creating new {@code PinnedThread} instances
     * @param affectGoverned     Whether or not the provide {@code AffinityDescriptor} should be applied to the
     *                           currently governed {@code PinnedThread} set
     */
    public void alter(
            final @Nonnull AffinityDescriptor affinityDescriptor,
            final boolean affectGoverned) {
        this.affinity = affinityDescriptor;
        if (affectGoverned) {
            alter(pinned -> pinned.affinity(affinityDescriptor));
        }
    }

    @Override
    public PinnedThread newThread(final @Nonnull Runnable r) {
        pinnedThreadStartLatch.await(true);
        val pinned = pinned(r);
        safe(() -> governed.add(pinned));
        return pinned;
    }

    private void safe(final @Nonnull Runnable action) {
        accessLock.lock();
        try {
            action.run();
        } finally {
            accessLock.unlock();
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
        pinnedThreadStartLatch.await(false);
        safe(() -> governed.forEach(fn));
    }

    private final class GovernedPinnedThread extends PinnedThread {

        GovernedPinnedThread(final @Nonnull Runnable target, @Nonnull final AffinityDescriptor descriptor) {
            super(target, descriptor);
        }

        GovernedPinnedThread(final @Nonnull Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            try {
                pinnedThreadStartLatch.fire();
                super.run();
            } finally {
                safe(() -> governed.remove(this));
            }
        }
    }
}
