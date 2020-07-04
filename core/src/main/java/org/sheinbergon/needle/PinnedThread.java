package org.sheinbergon.needle;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Accessors(fluent = true)
public class PinnedThread extends Thread {

    @Getter
    private volatile Object nativeId = null;
    @Nullable
    private Initializer initializer = null;

    public PinnedThread(final @Nonnull Runnable target) {
        super(target);
    }

    public PinnedThread(final @Nonnull Runnable target, final long mask) {
        super(target);
        initializer = () -> affinity(mask);
    }

    public PinnedThread(final @Nonnull Runnable target, final @Nonnull String mask) {
        super(target);
        initializer = () -> affinity(mask);
    }

    protected PinnedThread(final long mask) {
        super();
        initializer = () -> affinity(mask);
    }

    protected PinnedThread(final String mask) {
        super();
        initializer = () -> affinity(mask);
    }

    public final AffinityDescriptor affinity() {
        ensureInitialization();
        return Needle.affinity(this);
    }

    public final void affinity(final @Nonnull String mask) {
        ensureInitialization();
        Needle.affinity(mask, this);
    }

    public final void affinity(final long mask) {
        ensureInitialization();
        Needle.affinity(mask, this);
    }

    protected final void initialize() {
        nativeId = Needle.self();
        Objects.requireNonNull(nativeId);
        if (initializer != null) initializer.invoke();
    }

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

    @FunctionalInterface
    private interface Initializer {
        void invoke();
    }
}