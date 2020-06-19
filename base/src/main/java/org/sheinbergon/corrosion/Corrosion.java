package org.sheinbergon.corrosion;

import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.corrosion.jna.linux.LinuxAffinityResolver;
import org.sheinbergon.corrosion.jna.win32.Win32AffinityResolver;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes", "unchecked"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Corrosion {

    private static final AffinityResolver affinityResolver;

    static {
        if (Platform.isWindows()) {
            affinityResolver = Win32AffinityResolver.INSTANCE;
        } else if (Platform.isLinux()) {
            affinityResolver = LinuxAffinityResolver.INSTANCE;
        } else {
            affinityResolver = AffinityResolver.NoOp.INSTANCE;
        }
    }

    public interface AffinityResolver<I> {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        final class NoOp implements AffinityResolver<Object> {

            public static AffinityResolver<?> INSTANCE = new NoOp();

            @Nonnull
            @Override
            public Object self() {
                return NumberUtils.LONG_MINUS_ONE;
            }

            @Override
            public void set(@Nonnull CoreSet cores) {
            }

            @Override
            public void set(@Nonnull CoreSet cores, @Nonnull Object identifier) {
            }

            @Nonnull
            @Override
            public CoreSet get() {
                return CoreSet.EMPTY;
            }

            @Nonnull
            @Override
            public CoreSet get(@Nonnull Object identifier) {
                return CoreSet.EMPTY;
            }
        }

        @Nonnull
        I self();

        void set(@Nonnull CoreSet cores);

        void set(@Nonnull CoreSet cores, @Nonnull I identifier);

        @Nonnull
        CoreSet get();

        @Nonnull
        CoreSet get(@Nonnull I identifier);
    }

    public static CoreSet set(final long mask) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores);
        return cores;
    }

    public static CoreSet set(final @Nonnull String mask) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores);
        return cores;
    }

    public static CoreSet get() {
        return affinityResolver.get();
    }

    static void set(final long mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores, corroded.nativeId());
    }

    static void set(final @Nonnull String mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores, corroded.nativeId());
    }

    static CoreSet get(final @Nonnull Corroded corroded) {
        return affinityResolver.get(corroded.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }
}