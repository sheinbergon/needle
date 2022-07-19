package org.sheinbergon.needle;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Needle {

  /**
   * The facade used for various affinity tasks.
   */
  private static final AffinityResolver RESOLVER = AffinityResolver.INSTANCE;

  /**
   * Set the calling {@code Thread}'s affinity setting.
   *
   * @param affinityDescriptor the new affinity setting to apply
   */
  public static void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
    RESOLVER.thread(affinityDescriptor);
  }

  /**
   * Get the calling {@code Thread}'s affinity setting.
   *
   * @return the current affinity setting
   */
  public static AffinityDescriptor affinity() {
    return RESOLVER.thread();
  }

  static void affinity(
      final @Nonnull AffinityDescriptor affinity,
      final @Nonnull Object nativeId) {
    RESOLVER.thread(nativeId, affinity);
  }

  static AffinityDescriptor affinity(final @Nonnull Object nativeId) {
    return RESOLVER.thread(nativeId);
  }

  static Object self() {
    return RESOLVER.self();
  }

  private Needle() {
  }
}
