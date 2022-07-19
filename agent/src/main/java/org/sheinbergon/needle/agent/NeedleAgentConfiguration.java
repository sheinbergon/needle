package org.sheinbergon.needle.agent;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.math.NumberUtils;
import org.sheinbergon.needle.AffinityDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@Accessors(fluent = true)
public class NeedleAgentConfiguration {

  /**
   * Default configuration constructs, implies no-op affinity descriptor to the Needle framework.
   * To be used in the absence of needle-agent configuration specification.
   */
  public static final NeedleAgentConfiguration DEFAULT = new NeedleAgentConfiguration()
      .defaultAffinity(AffinityDescriptor.from(NumberUtils.LONG_ZERO));
  /**
   * A collection of affinity group descriptors used to match different affinity descriptors to threads upon
   * instantiation.
   */
  @Nullable
  private List<AffinityGroup> affinityGroups;
  /**
   * The default affinity to use for all threads without a precise {@link AffinityGroup} match.
   */
  @Nonnull
  private AffinityDescriptor defaultAffinity;

  @Data
  @NoArgsConstructor
  @Accessors(fluent = true, chain = true)
  public static final class AffinityGroup {

    /**
     * This group inflated {@link AffinityDescriptor}, to be used to apply affinity settings via {@code Needle}.
     *
     * @see org.sheinbergon.needle.Needle
     */
    @Nonnull
    private AffinityDescriptor affinity;
    /**
     * The match target qualifier, used to extract the match target string from a given {@code Thread}.
     */
    @Nullable
    private Qualifier qualifier;
    /**
     * The matching logic encapsulation (determined upon deserialization).
     */
    @Nullable
    private Matcher matcher;
    /**
     * The affinity group's identifier, meant to be used for descriptive purposes only.
     */
    @Nonnull
    private String identifier;

    /**
     * @param target the match target to be matched using this affinity group's {@link AffinityGroup#matcher}.
     * @return A boolean value indicating whether or not this group matches the given target or not.
     */
    public boolean matches(final @Nonnull String target) {
      return matcher.matches(target);
    }

    public enum Qualifier {

      /**
       * Thread name based matching qualifier, as provided by {@link Thread#getName()}.
       */
      NAME,
      /**
       * Thread class FQDN based matching qualifier, as provided by {@link Class#getName()}.
       */
      CLASS;
    }

    @Accessors(fluent = true, chain = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Matcher.Prefix.class, name = "PREFIX"),
        @JsonSubTypes.Type(value = Matcher.Regex.class, name = "REGEX")
    })
    public interface Matcher {

      /**
       * @param target the match target to be matched
       * @return Boolean value indicating whether or not this {@code Matcher} implementation matches the given
       * match target.
       */
      boolean matches(@Nonnull String target);

      @Data
      @NoArgsConstructor
      @Accessors(fluent = true, chain = true)
      @EqualsAndHashCode(callSuper = false)
      final class Prefix implements Matcher {

        /**
         * Specifies the string prefix used to match given target strings.
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
         * Specifies the regex expression used to match given target strings.
         */
        @Nonnull
        private Pattern pattern;

        @Override
        public boolean matches(final @Nonnull String target) {
          return pattern.matcher(target).matches();
        }
      }
    }
  }
}
