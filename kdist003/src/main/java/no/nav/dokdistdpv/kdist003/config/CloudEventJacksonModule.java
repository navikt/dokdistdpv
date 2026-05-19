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
import java.util.Set;

@JacksonComponent
public class CloudEventJacksonModule {

	private static final Set<String> STANDARD_ATTRIBUTES = Set.of(
			"id", "source", "type", "time", "subject", "data",
			"datacontenttype", "dataschema", "specversion",
			"extensionAttributes", "isValid", "specVersion", "dataContentType"
	);

	public static class Serializer extends StdSerializer<CloudEvent> {

		public Serializer() {
			super(CloudEvent.class);
		}

		@Override
		public void serialize(CloudEvent cloudEvent, JsonGenerator gen, SerializationContext ctxt) {
			gen.writeStartObject();

			if (cloudEvent.getId() != null) {
				gen.writeStringProperty("id", cloudEvent.getId());
			}
			if (cloudEvent.getSource() != null) {
				gen.writeStringProperty("source", cloudEvent.getSource().toString());
			}
			if (cloudEvent.getType() != null) {
				gen.writeStringProperty("type", cloudEvent.getType());
			}
			if (cloudEvent.getTime() != null) {
				gen.writeStringProperty("time", cloudEvent.getTime().toString());
			}
			if (cloudEvent.getSubject() != null) {
				gen.writeStringProperty("subject", cloudEvent.getSubject());
			}
			if (cloudEvent.getDataContentType() != null) {
				gen.writeStringProperty("datacontenttype", cloudEvent.getDataContentType());
			}
			if (cloudEvent.getDataSchema() != null) {
				gen.writeStringProperty("dataschema", cloudEvent.getDataSchema().toString());
			}

			if (cloudEvent.getSpecVersion() != null && cloudEvent.getSpecVersion().getVersionId() != null) {
				gen.writeStringProperty("specversion", cloudEvent.getSpecVersion().getVersionId());
			}

			if (cloudEvent.getExtensionAttributes() != null) {
				for (CloudEventAttribute attr : cloudEvent.getExtensionAttributes()) {
					if (attr.getName() != null && attr.getType() != null && attr.getType().getName() != null) {
						gen.writeStringProperty(attr.getName(), attr.getType().getName());
					}
				}
			}

			gen.writeEndObject();
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
			cloudEvent.setId(textOrNull(node, "id"));
			cloudEvent.setSource(uriOrNull(node, "source"));
			cloudEvent.setType(textOrNull(node, "type"));
			cloudEvent.setTime(offsetDateTimeOrNull(node, "time"));
			cloudEvent.setSubject(textOrNull(node, "subject"));
			cloudEvent.setDataContentType(textOrNull(node, "datacontenttype"));
			cloudEvent.setDataSchema(uriOrNull(node, "dataschema"));

			List<CloudEventAttribute> extensionAttributes = new ArrayList<>();

			Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String fieldName = field.getKey();
				if (!STANDARD_ATTRIBUTES.contains(fieldName) && field.getValue().isValueNode()) {
					String value = field.getValue().isNull() ? null : field.getValue().asText();
					extensionAttributes.add(createExtensionAttribute(fieldName, value));
				}
			}

			if (node.has("specversion")) {
				CloudEventsSpecVersion specVersion = new CloudEventsSpecVersion();
				specVersion.setVersionId(node.get("specversion").asText());
				cloudEvent.setSpecVersion(specVersion);
			}

			cloudEvent.setExtensionAttributes(extensionAttributes);
			return cloudEvent;
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
			return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
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
