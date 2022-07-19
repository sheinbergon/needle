package org.sheinbergon.needle.agent.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.agent.NeedleAgentConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public final class YamlCodec {

  /**
   * Configuration deserialization jackson {@link ObjectReader} settings.
   */
  private static final ObjectReader JACKSON = new ObjectMapper(new YAMLFactory())
      .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
      .addMixIn(AffinityDescriptor.class, AffinityDescriptorMixIn.class)
      .addMixIn(Pattern.class, RegexPatternMixIn.class)
      .readerFor(NeedleAgentConfiguration.class);

  private YamlCodec() {
  }

  /**
   * @param url Agent configuration file URL.
   * @return Deserialized {@link NeedleAgentConfiguration} instance.
   * @throws NeedleAgentException - in case of configuration yaml parsing error.
   */
  @Nonnull
  public static NeedleAgentConfiguration parseConfiguration(final @Nonnull URL url) throws NeedleException {
    try {
      return JACKSON.readValue(url);
    } catch (IOException iox) {
      throw new NeedleAgentException(iox);
    }
  }

  @JsonDeserialize(using = AffinityDescriptorDeserializer.class)
  private interface AffinityDescriptorMixIn {
  }

  @JsonDeserialize(using = RegexPatternDeserializer.class)
  private interface RegexPatternMixIn {
  }

  private static class AffinityDescriptorDeserializer extends JsonDeserializer<AffinityDescriptor> {

    @Override
    public AffinityDescriptor deserialize(
        final JsonParser parser,
        final DeserializationContext context) throws IOException {
      val codec = parser.getCodec();
      val node = (JsonNode) codec.readTree(parser);
      if (node.isTextual()) {
        return AffinityDescriptor.from(node.asText());
      } else if (node.isIntegralNumber()) {
        return AffinityDescriptor.from(node.asLong());
      } else {
        throw new NeedleException(
            String.format("Unsupported affinity descriptor value node type - %s",
                node.getClass().getSimpleName()));
      }
    }
  }

  private static class RegexPatternDeserializer extends JsonDeserializer<Pattern> {

    @Override
    public Pattern deserialize(
        final JsonParser parser,
        final DeserializationContext context) throws IOException {
      val codec = parser.getCodec();
      val node = (JsonNode) codec.readTree(parser);
      return Pattern.compile(node.asText());
    }
  }
}
