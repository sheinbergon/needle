package org.sheinbergon.needle.shielding;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.needle.AffinityDescriptor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@Accessors(fluent = true)
public class ShieldingConfiguration {

    /**
     *
     */
    public static final ShieldingConfiguration DEFAULT = new ShieldingConfiguration()
            .affinityGroups(List.of(
                    new AffinityGroup()
                            .affinity(AffinityDescriptor.from(NumberUtils.INTEGER_ZERO))
                            .identifier("default")
                            .qualifier(AffinityGroup.Qualifier.NAME)
                            .matcher(new AffinityGroup.Matcher.Prefix()
                                    .prefix(StringUtils.EMPTY))));

    @Data
    @NoArgsConstructor
    @Accessors(fluent = true, chain = true)
    public static final class AffinityGroup {

        public enum Qualifier {

            /**
             * a.
             */
            NAME, CLASS;
        }

        @Accessors(fluent = true, chain = true)
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = Matcher.Prefix.class, name = "PREFIX"),
                @JsonSubTypes.Type(value = Matcher.Regex.class, name = "REGEX")
        })
        public interface Matcher {

            @Data
            @NoArgsConstructor
            @Accessors(fluent = true, chain = true)
            @EqualsAndHashCode(callSuper = false)
            final class Prefix implements Matcher {

                /**
                 * .
                 */
                @Nonnull
                private String prefix;

                @Override
                public boolean matches(final @Nonnull String target) {
                    return target.startsWith(prefix);
                }
            }

            @Data
            @NoArgsConstructor
            @Accessors(fluent = true, chain = true)
            @EqualsAndHashCode(callSuper = false)
            final class Regex implements Matcher {

                /**
                 * .
                 */
                @Nonnull
                private Pattern pattern;

                @Override
                public boolean matches(final @Nonnull String target) {
                    return pattern.matcher(target).matches();
                }
            }

            /**
             * @param target
             * @return das
             */
            boolean matches(@Nonnull String target);
        }

        /**
         * .
         */
        @Nonnull
        private AffinityDescriptor affinity;
        /**
         * .
         */
        @Nonnull
        private Qualifier qualifier;
        /**
         * .
         */
        @Nonnull
        private Matcher matcher;
        /**
         * .
         */
        @Nonnull
        private String identifier;


        /**
         * @param target
         * @return dsa
         */
        public boolean matches(final @Nonnull String target) {
            return matcher.matches(target);
        }
    }

    /**
     * .
     */
    @Nonnull
    private List<AffinityGroup> affinityGroups;
}
