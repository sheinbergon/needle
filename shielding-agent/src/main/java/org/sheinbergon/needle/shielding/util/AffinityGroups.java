package org.sheinbergon.needle.shielding.util;

import com.google.common.collect.Sets;
import lombok.val;
import org.sheinbergon.needle.shielding.ShieldingConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
     * d.
     *
     * @param groups
     */
    public static void populate(final @Nonnull Collection<ShieldingConfiguration.AffinityGroup> groups) {
        for (ShieldingConfiguration.AffinityGroup group : groups) {
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

    /**
     * fd.
     *
     * @param thread
     * @return a
     */
    public static Optional<ShieldingConfiguration.AffinityGroup> forThread(final @Nonnull Thread thread) {
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

    private AffinityGroups() {
    }
}
