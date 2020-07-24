package org.sheinbergon.needle;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Accessors(fluent = true)
public class PinnedThread extends Thread {

    @FunctionalInterface
    private interface Initializer {
        void invoke();
    }

    /**
     * The native platform thread identifier use for various affinity operations.
     * <p>
     * Note: {@code Object} is used here instead of a concrete type to allow simpler cross platform variance.
     */
    @Getter
    private volatile Object nativeId = null;

    /**
     * Initialization callback reference, will be used for affinity assignment call
     * at the start of {@link PinnedThread#run()} execution.
     */
    @Nullable
    private Initializer initializer = null;

    /**
     * Initialize a {@code PinnedThread} using the owning process affinity.
     *
     * @param target the {@code Runnable} to run using this a new {@code PinnedThread}
     */
    public PinnedThread(final @Nonnull Runnable target) {
        super(target);
    }

    /**
     * Initialize a named {@code PinnedThread} using the owning process affinity.
     *
     * @param target the {@code Runnable} to run using this a new {@code PinnedThread}
     * @param name   the name to use for th new {@code PinnedThread}
     */
    public PinnedThread(
            final @Nonnull Runnable target,
            final @Nonnull String name) {
        super(target, name);
    }

    /**
     * Initialize a {@code PinnedThread} using a the specified affinity.
     *
     * @param target   the {@code Runnable} to run using this a new {@code PinnedThread}
     * @param affinity the {@code AffinityDescriptor} to use for the new {@code PinnedThread}
     */
    public PinnedThread(
            final @Nonnull Runnable target,
            final @Nonnull AffinityDescriptor affinity) {
        super(target);
        initializer = () -> affinity(affinity);
    }

    /**
     * Initialize a named {@code PinnedThread} using a the specified affinity.
     *
     * @param target   the {@code Runnable} to run using this a new {@code PinnedThread}
     * @param name     the name to use for th new {@code PinnedThread}
     * @param affinity the {@code AffinityDescriptor} to use for the new {@code PinnedThread}
     */
    public PinnedThread(
            final @Nonnull Runnable target,
            final @Nonnull String name,
            final @Nonnull AffinityDescriptor affinity) {
        super(target, name);
        initializer = () -> affinity(affinity);
    }

    protected PinnedThread(final @Nonnull AffinityDescriptor affinity) {
        super();
        initializer = () -> affinity(affinity);
    }

    /**
     * Get this {@code PinnedThread} affinity setting.
     *
     * @return the current affinity setting
     */
    public final AffinityDescriptor affinity() {
        ensureInitialization();
        return Needle.affinity(this);
    }

    /**
     * Set this {@code PinnedThread} affinity setting.
     *
     * @param affinityDescriptor the new affinity setting to apply
     */
    public final void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
        ensureInitialization();
        Needle.affinity(affinityDescriptor, this);
    }

    protected final void initialize() {
        nativeId = Needle.self();
        Objects.requireNonNull(nativeId);
        if (initializer != null) {
            initializer.invoke();
        }
    }

    /**
     *
     */
    @Override
    public void run() {
        initialize();
        super.run();
    }

    private void ensureInitialization() {
        if (nativeId == null) {
            throw new NeedleException("Pinned uninitialized, cannot access affinity information");
        }
    }
}
