package org.sheinbergon.needle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Needle {

    private static final AffinityResolver affinityResolver = AffinityResolver.instance;

    public static void affinity(final long mask) {
        val cores = AffinityDescriptor.from(mask);
        affinityResolver.thread(cores);
    }

    public static void affinity(final @Nonnull String mask) {
        val cores = AffinityDescriptor.from(mask);
        affinityResolver.thread(cores);
    }

    public static AffinityDescriptor affinity() {
        return affinityResolver.thread();
    }

    static void affinity(final long mask, final @Nonnull PinnedThread pinned) {
        val cores = AffinityDescriptor.from(mask);
        affinityResolver.thread(pinned.nativeId(), cores);
    }

    static void affinity(final @Nonnull String mask, final @Nonnull PinnedThread pinned) {
        val cores = AffinityDescriptor.from(mask);
        affinityResolver.thread(pinned.nativeId(), cores);
    }

    static AffinityDescriptor affinity(final @Nonnull PinnedThread pinned) {
        return affinityResolver.thread(pinned.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }
}