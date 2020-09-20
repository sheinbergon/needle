package org.sheinbergon.needle.shielding.util;

import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.sheinbergon.needle.shielding.ShieldingConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class AffinityGroups {

    /**
     *
     */
    private static final String DEFAULT_AFFINITY_GROUP_IDENTIFIER = "default";

    /**
     * a.
     */
    private static final List<ShieldingConfiguration.AffinityGroup> CLASS_MATCHING_AFFINITY_GROUPS =
            Lists.newArrayList();

    /**
     * a.
     */
    private static final List<ShieldingConfiguration.AffinityGroup> NAME_MATCHING_AFFINITY_GROUPS =
            Lists.newArrayList();

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
     * a.
     */
    private static ShieldingConfiguration.AffinityGroup defaultAffinityGroup = null;

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
    @Nonnull
    public static ShieldingConfiguration.AffinityGroup forThread(final @Nonnull Thread thread) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        return forThreadName(thread)
                .or(() -> forThreadClass(thread))
                .orElse(defaultAffinityGroup);
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
            final @Nonnull Collection<ShieldingConfiguration.AffinityGroup> affinityGroups) {
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
        defaultAffinityGroup = defaultAffinityGroup(configuration);
        val affinityGroups = affinityGroups(configuration);
        for (ShieldingConfiguration.AffinityGroup group : affinityGroups) {
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

    @Nonnull
    private static ShieldingConfiguration.AffinityGroup defaultAffinityGroup(
            final @Nonnull ShieldingConfiguration configuration) {
        return new ShieldingConfiguration.AffinityGroup()
                .identifier(DEFAULT_AFFINITY_GROUP_IDENTIFIER)
                .affinity(configuration.defaultAffinity());
    }

    @Nonnull
    private static List<ShieldingConfiguration.AffinityGroup> affinityGroups(
            final @Nonnull ShieldingConfiguration configuration) {
        return ObjectUtils.defaultIfNull(configuration.affinityGroups(), List.of());
    }

    private AffinityGroups() {
    }
}
