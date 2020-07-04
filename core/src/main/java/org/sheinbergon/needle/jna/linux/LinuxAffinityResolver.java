package org.sheinbergon.needle.jna.linux;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.sheinbergon.needle.AffinityResolver;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinuxAffinityResolver extends AffinityResolver<Long> {

    public static final AffinityResolver<?> INSTANCE = new LinuxAffinityResolver();

    private static final int pid = Libc.getpid();

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
    public void thread(final @Nonnull Long pthreadId, final @Nonnull AffinityDescriptor cores) {
        val set = cpuSet();
        set.__bits[0] |= cores.mask();
        Libpthread.pthread_setaffinity_np(pthreadId, set.bytes(), set);
    }

    @Nonnull
    @Override
    public AffinityDescriptor thread(@Nonnull Long pthreadId) {
        val set = cpuSet();
        Libpthread.pthread_getaffinity_np(pthreadId, set.bytes(), set);
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