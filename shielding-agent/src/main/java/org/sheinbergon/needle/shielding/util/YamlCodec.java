package org.sheinbergon.needle.shielding.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.shielding.ShieldingConfiguration;
import org.sheinbergon.needle.util.NeedleException;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public final class YamlCodec {

    /**
     *
     */
    private static final ObjectReader JACKSON = new ObjectMapper(new YAMLFactory())
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .addMixIn(AffinityDescriptor.class, AffinityDescriptorMixIn.class)
            .addMixIn(Pattern.class, RegexPatternMixIn.class)
            .readerFor(ShieldingConfiguration.class);

    /**
     * @param url - fsf
     * @return fdsf
     * @throws NeedleException - fsd
     */
    @Nonnull
    public static ShieldingConfiguration parseConfiguration(final @Nonnull URL url) throws NeedleException {
        try {
            return JACKSON.readValue(url);
        } catch (IOException iox) {
            throw new RuntimeException(iox);
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

    private YamlCodec() {
    }
}
