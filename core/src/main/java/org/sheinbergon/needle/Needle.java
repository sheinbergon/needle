package org.sheinbergon.needle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Needle {

    private static final AffinityResolver affinityResolver = AffinityResolver.instance;

    public static void affinity(final @Nonnull AffinityDescriptor affinity) {
        affinityResolver.thread(affinity);
    }

    public static AffinityDescriptor affinity() {
        return affinityResolver.thread();
    }

    static void affinity(AffinityDescriptor affinity, final @Nonnull PinnedThread pinned) {
        affinityResolver.thread(pinned.nativeId(), affinity);
    }

    static AffinityDescriptor affinity(final @Nonnull PinnedThread pinned) {
        return affinityResolver.thread(pinned.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }
}