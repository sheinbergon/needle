package org.sheinbergon.corrosion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.sheinbergon.corrosion.util.CoreSetException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class CoreSet {

    private final static Pattern RANGE = Pattern.compile("^\\s*(\\d{1,3})\\s*-\\s*(\\d{1,3})\\s*$");
    private final static Pattern SINGLE = Pattern.compile("^\\s*(\\d{1,3})\\s*$");

    private final static String DELIMITER = ",";

    public static CoreSet from(final long mask) {
        val specifications = new HashSet<Specification>();
        Integer start = null, end = null;
        for (var core = 0; core < Long.SIZE; core++) {
            if (((1L << core) & mask) > 0) {
                if (start == null)
                    start = core;
                end = core;
            } else {
                if (start != null && end != null) {
                    specifications.add(Specification.from(start, end));
                    end = start = null;
                }
            }
        }
        return new CoreSet(specifications);
    }

    public static CoreSet from(String mask) {
        val specifications = Arrays.stream(StringUtils.split(mask, DELIMITER))
                .map(CoreSet::specificationFrom)
                .collect(Collectors.toSet());
        return new CoreSet(specifications);
    }

    private static Specification specificationFrom(@Nonnull String specification) {
        int start, end;
        Matcher matcher;
        if ((matcher = RANGE.matcher(specification)).matches()) {
            start = Integer.parseInt(matcher.group(1));
            end = Integer.parseInt(matcher.group(2));
        } else if ((matcher = SINGLE.matcher(specification)).matches()) {
            start = end = Integer.parseInt(matcher.group());
        } else {
            throw new CoreSetException(String.format("Illegal core specification - '%s'", specification));
        }
        return Specification.from(start, end);
    }

    @Nonnull
    private final Set<Specification> specifications;

    public CoreSet(Specification... specifications) {
        this.specifications = Set.of(specifications);
    }

    @Override
    public String toString() {
        return specifications.stream()
                .map(Specification::toString)
                .sorted()
                .collect(Collectors.joining(DELIMITER));
    }

    @Getter(lazy = true)
    private final long mask = computeMask();

    private long computeMask() {
        return Optional.ofNullable(specifications).map(Set::stream)
                .map(stream -> stream.map(Specification::mask))
                .flatMap(stream -> stream.reduce((m1, m2) -> m1 | m2))
                .orElseThrow(() -> new IllegalArgumentException("Empty core set"));
    }

    public interface Specification {

        private static Specification from(int start, int end) {
            val diff = end - start;
            if (diff > 0) {
                return new Specification.Range(start, end);
            } else if (diff == 0) {
                return new Specification.Single(end);
            } else {
                throw new CoreSetException(String.format("Invalid core range, starts at %d, ends at %d", start, end));
            }
        }

        long mask();

        @RequiredArgsConstructor
        @Accessors(fluent = true)
        public class Single implements Specification {
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
        public class Range implements Specification {
            private final int start;
            private final int end;

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
                return String.format("%d-%d", start, end);
            }
        }
    }
}
