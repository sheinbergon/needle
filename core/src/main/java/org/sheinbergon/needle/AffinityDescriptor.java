package org.sheinbergon.needle;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.needle.util.AffinityDescriptorException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Accessors(fluent = true)
public class AffinityDescriptor {

    /**
     * Available core count as detected by the JVM.
     */
    @VisibleForTesting
    static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * Binary affinity mask upper bound, set according to {@link AffinityDescriptor#AVAILABLE_CORES}.
     */
    @VisibleForTesting
    static final int MASK_UPPER_BOUND = (int) Math.pow(2, AVAILABLE_CORES);

    /**
     * Textual mask core specification delimiter char.
     */
    @VisibleForTesting
    static final String SPECIFICATION_DELIMITER = ",";


    /**
     * Textual mask core range specification char.
     */
    @VisibleForTesting
    static final String RANGE_DELIMITER = "-";

    /**
     * This {@code AffinityDescriptor} instance is used when the underlying OS is
     * not supported by this library.
     */
    @VisibleForTesting
    static final AffinityDescriptor UNSUPPORTED = new AffinityDescriptor() {
        @Override
        public long mask() {
            return NumberUtils.LONG_MINUS_ONE;
        }

        @Override
        public String toString() {
            return null;
        }
    };

    /**
     * Regex pattern for core range specification.
     */
    private static final Pattern RANGE = Pattern.compile("^\\s*(\\d{1,3})\\s*-\\s*(\\d{1,3})\\s*$");

    /**
     * Regex pattern for single core specification.
     */
    private static final Pattern SINGLE = Pattern.compile("^\\s*(\\d{1,3})\\s*$");

    /**
     * Zero or more affinity {@code Specification} the {@code AffinityDescriptor} is comprised of.
     */
    @Nonnull
    private final Set<Specification> specifications;

    /**
     * The computed binary affinity mask computed from the given {@link AffinityDescriptor#specifications}.
     */
    @Getter(lazy = true)
    private final long mask = computeMask();

    AffinityDescriptor(final Specification... affinitySpecifications) {
        this.specifications = Set.of(affinitySpecifications);
    }

    static AffinityDescriptor process() {
        return AffinityResolver.instance.process();
    }

    /**
     * Factory method to instantiate an {@code AffinityDescriptor} from a given binary mask.
     *
     * @param mask the binary mask affinity specification
     * @return an {@code AffinityDescriptor} matching the given binary mask
     */
    public static AffinityDescriptor from(final long mask) {
        val specifications = new HashSet<Specification>();
        Integer start = null, end = null;
        for (var core = 0; core < Long.SIZE; core++) {
            if (((1L << core) & mask) > 0) {
                if (start == null) {
                    start = core;
                }
                end = core;
            } else {
                if (start != null && end != null) {
                    specifications.add(Specification.from(start, end));
                    end = start = null;
                }
            }
        }
        return from(specifications);
    }

    /**
     * Factory method to instantiate an {@code AffinityDescriptor} from a given textual mask.
     *
     * @param mask the textual mask affinity specification
     * @return an {@code AffinityDescriptor} matching the given textual mask
     */
    public static AffinityDescriptor from(final @Nonnull String mask) {
        val specifications = Arrays.stream(StringUtils.split(mask, SPECIFICATION_DELIMITER))
                .filter(Predicate.not(String::isBlank))
                .map(AffinityDescriptor::specificationFrom)
                .collect(Collectors.toSet());
        return from(specifications);
    }

    private static AffinityDescriptor from(final @Nonnull Set<Specification> specifications) {
        if (specifications.isEmpty()) {
            return process();
        } else {
            val descriptor = new AffinityDescriptor(specifications.toArray(Specification[]::new));
            validate(descriptor.mask());
            return descriptor;
        }
    }

    private static Specification specificationFrom(final @Nonnull String specification) {
        int start;
        int end;
        Matcher matcher;
        if ((matcher = RANGE.matcher(specification)).matches()) {
            start = Integer.parseInt(matcher.group(1));
            end = Integer.parseInt(matcher.group(2));
        } else if ((matcher = SINGLE.matcher(specification)).matches()) {
            start = end = Integer.parseInt(matcher.group());
        } else {
            throw new AffinityDescriptorException(String.format("Illegal specification - '%s'", specification));
        }
        return Specification.from(start, end);
    }

    private static void validate(final long mask) {
        if (mask <= 0 || mask > AffinityDescriptor.MASK_UPPER_BOUND) {
            throw new AffinityDescriptorException(
                    String.format("Mask %d is out of bounds, only %d cores are available",
                            mask, AffinityDescriptor.AVAILABLE_CORES));
        }
    }

    /**
     * Generates a textual representation of the affinity mask described by this {@code AffinityDescriptor}.
     */
    @Override
    public String toString() {
        return specifications.stream()
                .map(Specification::toString)
                .sorted()
                .collect(Collectors.joining(SPECIFICATION_DELIMITER));
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(mask());
    }

    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (!(other instanceof AffinityDescriptor)) {
            return false;
        } else {
            return ((AffinityDescriptor) other).mask() == this.mask();
        }
    }

    private long computeMask() {
        return Optional.ofNullable(specifications).map(Set::stream)
                .map(stream -> stream.map(Specification::mask))
                .flatMap(stream -> stream.reduce((m1, m2) -> m1 | m2))
                .orElse(NumberUtils.LONG_ZERO);
    }

    private interface Specification {

        private static Specification from(int start, int end) {
            val diff = end - start;
            if (diff > 0) {
                return new Specification.Range(start, end);
            } else if (diff == 0) {
                return new Specification.Single(end);
            } else {
                throw new AffinityDescriptorException(
                        String.format("Invalid core range, starts at %d, ends at %d", start, end));
            }
        }

        long mask();

        @RequiredArgsConstructor
        @Accessors(fluent = true)
        class Single implements Specification {
            /**
             * Single core numeric index.
             */
            private final int core;

            @Override
            public long mask() {
                return 1 << core;
            }

            @Override
            public String toString() {
                return Integer.toString(core);
            }
        }

        @RequiredArgsConstructor
        @Accessors(fluent = true)
        class Range implements Specification {
            /**
             * Core range (inclusive) start numeric index.
             */
            private final int start;

            /**
             * Core range (inclusive) end numeric index.
             */
            private final int end;


            /**
             * The computed binary affinity mask computed from the specified range.
             */
            @Getter(lazy = true)
            private final long mask = computeMask();

            private long computeMask() {
                return IntStream.rangeClosed(start, end)
                        .map(core -> 1 << core)
                        .reduce((c1, c2) -> c1 | c2)
                        .orElseThrow(() -> new IllegalArgumentException("Illegal range specified"));
            }

            @Override
            public String toString() {
                return String.format("%d%s%d", start, RANGE_DELIMITER, end);
            }
        }
    }
}
