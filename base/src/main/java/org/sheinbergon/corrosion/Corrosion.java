package org.sheinbergon.corrosion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Corrosion {

    private static final AffinityResolver affinityResolver = AffinityResolver.instance;

    public static void set(final long mask) {
        val cores = CoreSet.from(mask);
        affinityResolver.thread(cores);
    }

    public static void set(final @Nonnull String mask) {
        val cores = CoreSet.from(mask);
        affinityResolver.thread(cores);
    }

    public static CoreSet get() {
        return affinityResolver.thread();
    }

    static void set(final long mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.thread(corroded.nativeId(), cores);
    }

    static void set(final @Nonnull String mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.thread(corroded.nativeId(), cores);
    }

    static CoreSet get(final @Nonnull Corroded corroded) {
        return affinityResolver.thread(corroded.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }
}