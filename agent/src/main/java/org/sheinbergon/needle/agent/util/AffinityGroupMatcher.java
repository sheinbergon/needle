package org.sheinbergon.needle.agent.util;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.sheinbergon.needle.agent.NeedleAgentConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class AffinityGroupMatcher {

    /**
     * Generated default {@link org.sheinbergon.needle.agent.NeedleAgentConfiguration.AffinityGroup} identifier.
     */
    private static final String DEFAULT_AFFINITY_GROUP_IDENTIFIER = "default";

    /**
     * Thread Class FQDN based matching affinity groups.
     */
    private static final List<NeedleAgentConfiguration.AffinityGroup> CLASS_MATCHING_AFFINITY_GROUPS =
            Lists.newArrayList();

    /**
     * Thread name based matching affinity groups.
     */
    private static final List<NeedleAgentConfiguration.AffinityGroup> NAME_MATCHING_AFFINITY_GROUPS =
            Lists.newArrayList();

    /**
     * {@link NeedleAgentConfiguration} supplier.
     * <p>
     * Note: Due to bootstrapping agent class loading concerns, we use this supplier to defer configuration
     * deserialization to ongoing JVM {@code Thread} instantiation.
     *
     * @see YamlCodec
     * @see org.sheinbergon.needle.agent.NeedleAgent
     */
    @Setter
    @Nonnull
    private static volatile Supplier<NeedleAgentConfiguration> configurationSupplier =
            () -> NeedleAgentConfiguration.DEFAULT;

    /**
     * This flag is used to ensure configuration is deserialized only once, during the first instantiated thread
     * startup.
     */
    private static volatile boolean initialized = false;


    /**
     * This variable contains the default (fallback) affinity group, used to match affinity to all threads without
     * an precisely matched affinity group.
     */
    private static NeedleAgentConfiguration.AffinityGroup defaultAffinityGroup = null;

    /**
     * Match an affintiy group for a given {@code Thread} according to the following logic
     * <p>
     * 1. Initialize affinity group constructors/data, if not previously initialized.
     * 2. Try and find a thread-name based matching affinity group.
     * 3. If no match was previously found, try and find a thread-class based matching affinity group.
     * 4. If no match was previosuly found, use the defauly affinity group.
     *
     * @param thread The thread for which an affinity group should be matched.
     * @return the matching {@link NeedleAgentConfiguration.AffinityGroup}.
     */
    @Nonnull
    public static NeedleAgentConfiguration.AffinityGroup forThread(final @Nonnull Thread thread) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        return forThreadName(thread)
                .or(() -> forThreadClass(thread))
                .orElse(defaultAffinityGroup);
    }

    @Nonnull
    private static Optional<NeedleAgentConfiguration.AffinityGroup> forThreadName(final @Nonnull Thread thread) {
        val target = thread.getName();
        return forTarget(target, NAME_MATCHING_AFFINITY_GROUPS);
    }

    @Nonnull
    private static Optional<NeedleAgentConfiguration.AffinityGroup> forThreadClass(final @Nonnull Thread thread) {
        val target = thread.getClass().getName();
        return forTarget(target, CLASS_MATCHING_AFFINITY_GROUPS);
    }

    @Nonnull
    private static Optional<NeedleAgentConfiguration.AffinityGroup> forTarget(
            final @Nonnull String target,
            final @Nonnull Collection<NeedleAgentConfiguration.AffinityGroup> affinityGroups) {
        NeedleAgentConfiguration.AffinityGroup matched = null;
        for (NeedleAgentConfiguration.AffinityGroup group : affinityGroups) {
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
        for (NeedleAgentConfiguration.AffinityGroup group : affinityGroups) {
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
    private static NeedleAgentConfiguration.AffinityGroup defaultAffinityGroup(
            final @Nonnull NeedleAgentConfiguration configuration) {
        return new NeedleAgentConfiguration.AffinityGroup()
                .identifier(DEFAULT_AFFINITY_GROUP_IDENTIFIER)
                .affinity(configuration.defaultAffinity());
    }

    @Nonnull
    private static List<NeedleAgentConfiguration.AffinityGroup> affinityGroups(
            final @Nonnull NeedleAgentConfiguration configuration) {
        return ObjectUtils.defaultIfNull(configuration.affinityGroups(), List.of());
    }

    private AffinityGroupMatcher() {
    }
}
