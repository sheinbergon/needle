package org.sheinbergon.needle;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Needle {

    /**
     * The facade used for various affinity tasks.
     */
    private static final AffinityResolver affinityResolver = AffinityResolver.instance;

    /**
     * Set the calling {@code Thread}'s affinity setting.
     *
     * @param affinityDescriptor the new affinity setting to apply
     */
    public static void affinity(final @Nonnull AffinityDescriptor affinityDescriptor) {
        affinityResolver.thread(affinityDescriptor);
    }

    /**
     * Get the calling {@code Thread}'s affinity setting.
     *
     * @return the current affinity setting
     */
    public static AffinityDescriptor affinity() {
        return affinityResolver.thread();
    }

    static void affinity(
            final @Nonnull AffinityDescriptor affinity,
            final @Nonnull PinnedThread pinned) {
        affinityResolver.thread(pinned.nativeId(), affinity);
    }

    static AffinityDescriptor affinity(final @Nonnull PinnedThread pinned) {
        return affinityResolver.thread(pinned.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }

    private Needle() {
    }
}
