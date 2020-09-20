package org.sheinbergon.needle.shielding.util;

import com.google.common.collect.Sets;
import lombok.val;
import org.sheinbergon.needle.shielding.ShieldingConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class AffinityGroups {

    /**
     * a.
     */
    private static final Set<ShieldingConfiguration.AffinityGroup> CLASS_MATCHING_AFFINITY_GROUPS =
            Sets.newConcurrentHashSet();

    /**
     * a.
     */
    private static final Set<ShieldingConfiguration.AffinityGroup> NAME_MATCHING_AFFINITY_GROUPS =
            Sets.newConcurrentHashSet();

    /**
     * a.
     */
    @Nonnull
    private static volatile Supplier<ShieldingConfiguration> configurationSupplier =
            () -> ShieldingConfiguration.DEFAULT;

    /**
     * '.
     */
    private static volatile boolean initialized = false;

    /**
     * d.
     *
     * @param supplier
     */
    public static void setConfigurationSupplier(final @Nonnull Supplier<ShieldingConfiguration> supplier) {
        AffinityGroups.configurationSupplier = supplier;
    }

    /**
     * fd.
     *
     * @param thread
     * @return a
     */
    public static Optional<ShieldingConfiguration.AffinityGroup> forThread(final @Nonnull Thread thread) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        return forThreadName(thread).or(() -> forThreadClass(thread));
    }

    @Nonnull
    private static Optional<ShieldingConfiguration.AffinityGroup> forThreadName(final @Nonnull Thread thread) {
        val target = thread.getName();
        return forTarget(target, NAME_MATCHING_AFFINITY_GROUPS);
    }

    @Nonnull
    private static Optional<ShieldingConfiguration.AffinityGroup> forThreadClass(final @Nonnull Thread thread) {
        val target = thread.getClass().getName();
        return forTarget(target, CLASS_MATCHING_AFFINITY_GROUPS);
    }

    @Nonnull
    private static Optional<ShieldingConfiguration.AffinityGroup> forTarget(
            final @Nonnull String target,
            final @Nonnull Set<ShieldingConfiguration.AffinityGroup> affinityGroups) {
        ShieldingConfiguration.AffinityGroup matched = null;
        for (ShieldingConfiguration.AffinityGroup group : affinityGroups) {
            if (group.matches(target)) {
                matched = group;
                break;
            }
        }
        return Optional.ofNullable(matched);
    }

    private static void initialize() {
        val configuration = configurationSupplier.get();
        for (ShieldingConfiguration.AffinityGroup group : configuration.affinityGroups()) {
            val qualifier = group.qualifier();
            switch (qualifier) {
                case NAME:
                    NAME_MATCHING_AFFINITY_GROUPS.add(group);
                    break;
                case CLASS:
                    CLASS_MATCHING_AFFINITY_GROUPS.add(group);
                    break;
                default:
                    throw new NeedleException(
                            String.format("Unsupported affinity group qualifier '%s'",
                                    qualifier));
            }
        }
    }

    private AffinityGroups() {
    }
}
