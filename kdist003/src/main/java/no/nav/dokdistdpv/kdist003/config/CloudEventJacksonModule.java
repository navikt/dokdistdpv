package no.nav.dokdistdpv.kdist003.config;

import no.altinn.event.domain.CloudEvent;
import no.altinn.event.domain.CloudEventAttribute;
import no.altinn.event.domain.CloudEventAttributeType;
import no.altinn.event.domain.CloudEventsSpecVersion;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.std.StdSerializer;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JacksonComponent
public class CloudEventJacksonModule {

	private static final String ID = "id";
	private static final String SOURCE = "source";
	private static final String TYPE = "type";
	private static final String TIME = "time";
	private static final String SUBJECT = "subject";
	private static final String DATA = "data";
	private static final String DATA_CONTENT_TYPE = "datacontenttype";
	private static final String DATA_SCHEMA = "dataschema";
	private static final String SPEC_VERSION = "specversion";

	private static final Set<String> STANDARD_ATTRIBUTES = Set.of(
			ID, SOURCE, TYPE, TIME, SUBJECT, DATA,
			DATA_CONTENT_TYPE, DATA_SCHEMA, SPEC_VERSION,
			"extensionAttributes", "isValid", "dataContentType"
	);

	public static class Serializer extends StdSerializer<CloudEvent> {

		public Serializer() {
			super(CloudEvent.class);
		}

		@Override
		public void serialize(CloudEvent cloudEvent, JsonGenerator gen, SerializationContext ctxt) {
			gen.writeStartObject();
			writeStandardAttributes(cloudEvent, gen);
			writeExtensionAttributes(cloudEvent, gen);
			gen.writeEndObject();
		}

		private void writeStandardAttributes(CloudEvent cloudEvent, JsonGenerator gen) {
			writeIfPresent(gen, ID, cloudEvent.getId());
			writeIfPresent(gen, SOURCE, Objects.toString(cloudEvent.getSource(), null));
			writeIfPresent(gen, TYPE, cloudEvent.getType());
			writeIfPresent(gen, TIME, Objects.toString(cloudEvent.getTime(), null));
			writeIfPresent(gen, SUBJECT, cloudEvent.getSubject());
			writeIfPresent(gen, DATA_CONTENT_TYPE, cloudEvent.getDataContentType());
			writeIfPresent(gen, DATA_SCHEMA, Objects.toString(cloudEvent.getDataSchema(), null));

			if (cloudEvent.getSpecVersion() != null && cloudEvent.getSpecVersion().getVersionId() != null) {
				gen.writeStringProperty(SPEC_VERSION, cloudEvent.getSpecVersion().getVersionId());
			}
		}

		private void writeExtensionAttributes(CloudEvent cloudEvent, JsonGenerator gen) {
			if (cloudEvent.getExtensionAttributes() == null) {
				return;
			}
			for (CloudEventAttribute attr : cloudEvent.getExtensionAttributes()) {
				if (attr.getName() != null && attr.getType() != null && attr.getType().getName() != null) {
					gen.writeStringProperty(attr.getName(), attr.getType().getName());
				}
			}
		}

		private void writeIfPresent(JsonGenerator gen, String field, String value) {
			if (value != null) {
				gen.writeStringProperty(field, value);
			}
		}
	}

	public static class Deserializer extends StdDeserializer<CloudEvent> {

		public Deserializer() {
			super(CloudEvent.class);
		}

		@Override
		public CloudEvent deserialize(JsonParser p, DeserializationContext ctxt) {
			ObjectNode node = p.readValueAsTree();

			CloudEvent cloudEvent = new CloudEvent();
			deserializeStandardAttributes(node, cloudEvent);
			deserializeSpecVersion(node, cloudEvent);
			cloudEvent.setExtensionAttributes(deserializeExtensionAttributes(node));
			return cloudEvent;
		}

		private void deserializeStandardAttributes(ObjectNode node, CloudEvent cloudEvent) {
			cloudEvent.setId(textOrNull(node, ID));
			cloudEvent.setSource(uriOrNull(node, SOURCE));
			cloudEvent.setType(textOrNull(node, TYPE));
			cloudEvent.setTime(offsetDateTimeOrNull(node, TIME));
			cloudEvent.setSubject(textOrNull(node, SUBJECT));
			cloudEvent.setDataContentType(textOrNull(node, DATA_CONTENT_TYPE));
			cloudEvent.setDataSchema(uriOrNull(node, DATA_SCHEMA));
		}

		private void deserializeSpecVersion(ObjectNode node, CloudEvent cloudEvent) {
			if (node.has(SPEC_VERSION)) {
				CloudEventsSpecVersion specVersion = new CloudEventsSpecVersion();
				specVersion.setVersionId(node.get(SPEC_VERSION).asString());
				cloudEvent.setSpecVersion(specVersion);
			}
		}

		private List<CloudEventAttribute> deserializeExtensionAttributes(ObjectNode node) {
			List<CloudEventAttribute> extensionAttributes = new ArrayList<>();

			Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String fieldName = field.getKey();
				if (!STANDARD_ATTRIBUTES.contains(fieldName) && field.getValue().isValueNode()) {
					String value = field.getValue().isNull() ? null : field.getValue().asString();
					extensionAttributes.add(createExtensionAttribute(fieldName, value));
				}
			}
			return extensionAttributes;
		}

		private CloudEventAttribute createExtensionAttribute(String name, String value) {
			CloudEventAttributeType attributeType = new CloudEventAttributeType();
			attributeType.setName(value);

			CloudEventAttribute attribute = new CloudEventAttribute();
			attribute.setName(name);
			attribute.setType(attributeType);
			attribute.setIsExtension(true);
			attribute.setIsRequired(false);
			return attribute;
		}

		private String textOrNull(ObjectNode node, String field) {
			return node.has(field) && !node.get(field).isNull() ? node.get(field).asString() : null;
		}

		private URI uriOrNull(ObjectNode node, String field) {
			String text = textOrNull(node, field);
			return text != null ? URI.create(text) : null;
		}

		private OffsetDateTime offsetDateTimeOrNull(ObjectNode node, String field) {
			String text = textOrNull(node, field);
			return text != null ? OffsetDateTime.parse(text) : null;
		}
	}
}
