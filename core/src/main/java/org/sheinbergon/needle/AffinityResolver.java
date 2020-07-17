package org.sheinbergon.needle;

import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.needle.jna.linux.LinuxAffinityResolver;
import org.sheinbergon.needle.jna.win32.Win32AffinityResolver;

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

    final void thread(@Nonnull AffinityDescriptor affinity) {
        thread(self(), affinity);
    }

    protected abstract void thread(@Nonnull I identifier, @Nonnull AffinityDescriptor affinity);

    @Nonnull
    final AffinityDescriptor thread() {
        return thread(self());
    }

    @Nonnull
    protected abstract AffinityDescriptor process();

    @Nonnull
    protected abstract AffinityDescriptor thread(@Nonnull I identifier);

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
        protected AffinityDescriptor process() {
            return AffinityDescriptor.UNSUPPORTED;
        }

        @Override
        protected void thread(@Nonnull Object identifier, @Nonnull AffinityDescriptor affinity) {
        }

        @Nonnull
        @Override
        protected AffinityDescriptor thread(@Nonnull Object identifier) {
            return AffinityDescriptor.UNSUPPORTED;
        }
    }
}
