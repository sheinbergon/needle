package org.sheinbergon.needle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public interface Pinned {

  /**
   * Get the current `Pinned` if executing from within one.
   *
   * @return an {@code Optional} containing the current {@link Pinned},
   * or empty if the calling code does not run within one.
   */
  @Nonnull
  static Optional<Pinned> current() {
    return Optional.of(Thread.currentThread())
        .filter(Pinned.class::isInstance)
        .map(Pinned.class::cast);
  }

  @Accessors(fluent = true)
  final class Delegate implements Pinned {

    @FunctionalInterface
    interface Initializer {
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
     * Initialization callback reference, should be used for affinity assignment call
     * at the start of {@link Thread#run()} method execution.
     */
    @Nullable
    @Setter(AccessLevel.PACKAGE)
    private Initializer initializer = null;

    /**
     * Get this {@code PinnedThread} affinity setting.
     *
     * @return the current affinity setting
     */
    @Override
    public AffinityDescriptor affinity() {
      ensureInitialization();
      return Needle.affinity(nativeId);
    }

    /**
     * Set this {@code PinnedThread} affinity setting.
     *
     * @param affinityDescriptor the new affinity setting to apply
     */
    @Override
    public void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
      ensureInitialization();
      Needle.affinity(affinityDescriptor, nativeId);
    }

    void initialize() {
      nativeId = Needle.self();
      Objects.requireNonNull(nativeId);
      if (initializer != null) {
        initializer.invoke();
      }
    }

    private void ensureInitialization() {
      if (nativeId == null) {
        throw new NeedleException("native id uninitialized, cannot access affinity information");
      }
    }
  }

  /**
   * The native platform thread identifier use for various affinity operations.
   * <p>
   * Note: {@code Object} is used here instead of a concrete type to allow simpler cross platform variance.
   *
   * @return the platform thread identifier
   */
  Object nativeId();

  /**
   * Get this {@code PinnedThread} affinity setting.
   *
   * @return the current affinity setting
   */
  AffinityDescriptor affinity();

  /**
   * Set this {@code PinnedThread} affinity setting.
   *
   * @param affinityDescriptor the new affinity setting to apply
   */
  void affinity(@Nonnull AffinityDescriptor affinityDescriptor);
}
