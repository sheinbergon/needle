package org.sheinbergon.corrosion;

import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.corrosion.jna.linux.LinuxAffinityResolver;
import org.sheinbergon.corrosion.jna.win32.Win32AffinityResolver;

import javax.annotation.Nonnull;

@SuppressWarnings({"rawtypes"})
public abstract class AffinityResolver<I> {

    static final AffinityResolver instance;

    static {
        if (Platform.isWindows()) {
            instance = Win32AffinityResolver.INSTANCE;
        } else if (Platform.isLinux()) {
            instance = LinuxAffinityResolver.INSTANCE;
        } else {
            instance = AffinityResolver.NoOp.INSTANCE;
        }
    }

    @Nonnull
    protected abstract I self();

    final void thread(@Nonnull CoreSet cores) {
        thread(self(), cores);
    }

    protected abstract void thread(@Nonnull I identifier, @Nonnull CoreSet cores);

    @Nonnull
    final CoreSet thread() {
        return thread(self());
    }

    @Nonnull
    protected abstract CoreSet process();

    @Nonnull
    protected abstract CoreSet thread(@Nonnull I identifier);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class NoOp extends AffinityResolver<Object> {

        public static AffinityResolver<?> INSTANCE = new NoOp();

        @Nonnull
        @Override
        protected Object self() {
            return NumberUtils.LONG_MINUS_ONE;
        }

        @Nonnull
        @Override
        protected CoreSet process() {
            return CoreSet.UNSUPPORTED;
        }

        @Override
        protected void thread(@Nonnull Object identifier, @Nonnull CoreSet cores) {
        }

        @Nonnull
        @Override
        protected CoreSet thread(@Nonnull Object identifier) {
            return CoreSet.UNSUPPORTED;
        }
    }
}
