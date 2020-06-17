package org.sheinbergon.corrosion;

import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.sheinbergon.corrosion.jna.linux.LinuxAffinityResolver;
import org.sheinbergon.corrosion.jna.win32.Win32AffinityResolver;
import org.sheinbergon.corrosion.util.CorrosionException;

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
            throw new CorrosionException(String.format("Unsupported OS type - %d", Platform.getOSType()));
        }
    }

    public interface AffinityResolver<I> {

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

    static CoreSet set(final long mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores, corroded.nativeId());
        return cores;
    }

    static CoreSet set(final @Nonnull String mask, final @Nonnull Corroded corroded) {
        val cores = CoreSet.from(mask);
        affinityResolver.set(cores, corroded.nativeId());
        return cores;
    }

    static CoreSet get(final @Nonnull Corroded corroded) {
        return affinityResolver.get(corroded.nativeId());
    }

    static Object self() {
        return affinityResolver.self();
    }
}