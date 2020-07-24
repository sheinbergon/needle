package org.sheinbergon.needle.jna.linux;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.AffinityResolver;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinuxAffinityResolver extends AffinityResolver<Long> {

    /**
     * Singleton instance for {@code AffinityResolver<Long>} concrete Linux implementation.
     */
    public static final AffinityResolver<?> INSTANCE = new LinuxAffinityResolver();

    /**
     * Own OS process id (PID) determined during class-loading.
     */
    private final int pid = Libc.getpid();

    private static CpuSet cpuSet() {
        CpuSet set = new CpuSet();
        set.zero();
        return set;
    }

    @Nonnull
    @Override
    public Long self() {
        return Libpthread.pthread_self();
    }

    @Override
    public void thread(final @Nonnull Long identifier, final @Nonnull AffinityDescriptor affinity) {
        val set = cpuSet();
        set.__bits[0] |= affinity.mask();
        Libpthread.pthread_setaffinity_np(identifier, set.bytes(), set);
    }

    @Nonnull
    @Override
    public AffinityDescriptor thread(final @Nonnull Long identifier) {
        val set = cpuSet();
        Libpthread.pthread_getaffinity_np(identifier, set.bytes(), set);
        val mask = set.__bits[0];
        return AffinityDescriptor.from(mask);
    }

    @Nonnull
    @Override
    protected AffinityDescriptor process() {
        val set = cpuSet();
        Libc.sched_getaffinity(pid, set.bytes(), set);
        val mask = set.__bits[0];
        return AffinityDescriptor.from(mask);
    }
}
