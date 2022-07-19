package org.sheinbergon.needle;

import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@Accessors(fluent = true)
public class PinnedThread extends Thread implements Pinned {

  /**
   * This delegate is responsible for performing affinity related operations.
   */
  private final Pinned.Delegate delegate = new Pinned.Delegate();

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
    delegate.initializer(() -> affinity(affinity));
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
    delegate.initializer(() -> affinity(affinity));
  }

  protected PinnedThread(final @Nonnull AffinityDescriptor affinity) {
    super();
    delegate.initializer(() -> affinity(affinity));
  }

  @Override
  public final Object nativeId() {
    return delegate.nativeId();
  }

  @Override
  public final AffinityDescriptor affinity() {
    return delegate.affinity();
  }

  @Override
  public final void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
    delegate.affinity(affinityDescriptor);
  }

  final void initialize() {
    delegate.initialize();
  }

  /**
   * Ensure this thread's affinity is properly initialized prior to commencing its designated execution.
   */
  @Override
  public void run() {
    delegate.initialize();
    super.run();
  }

  @Accessors(fluent = true)
  public static class ForkJoinWorker extends ForkJoinWorkerThread implements Pinned {

    /**
     * This delegate is responsible for performing affinity related operations.
     */
    private final Pinned.Delegate delegate = new Pinned.Delegate();

    /**
     * Creates a {@code PinnedForkJoinWorkerThread} operating within the given pool with the given affinity setting.
     *
     * @param pool     the pool this thread works in
     * @param affinity The affinity setting for this thread
     */
    public ForkJoinWorker(
        final @Nonnull ForkJoinPool pool,
        final @Nonnull AffinityDescriptor affinity) {
      super(pool);
      delegate.initializer(() -> affinity(affinity));
    }

    /**
     * Creates a {@code PinnedForkJoinWorkerThread} operating within the given pool with the default affinity.
     *
     * @param pool the pool this thread works in
     */
    public ForkJoinWorker(final @Nonnull ForkJoinPool pool) {
      super(pool);
    }

    @Override
    public final Object nativeId() {
      return delegate.affinity();
    }

    @Override
    public final AffinityDescriptor affinity() {
      return delegate.affinity();
    }

    @Override
    public final void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
      delegate.affinity(affinityDescriptor);
    }

    /**
     * Ensures affinity initialization takes place prior to the processing of any submitted tasks.
     * <p>
     * If overriding this method, be sure to invoke {@code super.onStart()} before
     * executing any additional functionality.
     */
    @Override
    protected void onStart() {
      super.onStart();
      delegate.initialize();
    }

    /**
     * Ensure this thread's affinity is properly initialized prior to commencing its designated execution.
     */
    @Override
    public final void run() {
      super.run();
    }
  }
}
