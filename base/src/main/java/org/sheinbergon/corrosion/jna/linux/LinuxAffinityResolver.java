package org.sheinbergon.corrosion.jna.linux;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.sheinbergon.corrosion.CoreSet;
import org.sheinbergon.corrosion.Corrosion;
import org.sheinbergon.corrosion.jna.linux.structure.CpuSet;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinuxAffinityResolver implements Corrosion.AffinityResolver<Long> {

    public static final Corrosion.AffinityResolver<?> INSTANCE = new LinuxAffinityResolver();

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
    public void set(final @Nonnull CoreSet cores) {
        set(cores, self());
    }

    @Override
    public void set(final @Nonnull CoreSet cores, @Nonnull Long pthreadId) {
        val set = cpuSet();
        set.__bits[0] |= cores.mask();
        Libpthread.pthread_setaffinity_np(pthreadId, set.bytes(), set);
    }

    @Nonnull
    public CoreSet get() {
        return get(self());
    }

    @Nonnull
    @Override
    public CoreSet get(@Nonnull Long pthreadId) {
        val set = cpuSet();
        Libpthread.pthread_getaffinity_np(pthreadId, set.bytes(), set);
        val mask = set.__bits[0];
        return CoreSet.from(mask);
    }
}