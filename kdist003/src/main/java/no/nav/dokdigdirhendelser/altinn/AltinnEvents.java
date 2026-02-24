package no.nav.dokdigdirhendelser.altinn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Schema for Altinn Events from no.nav.dokdigdirhendelser.altinn package.
 * Generated from altinn-events-schema.json
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AltinnEvents(
        /**
         * Unique identifier for the event
         */
        @JsonProperty("id")
        UUID id,

        /**
         * The resource identifier (e.g., urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost)
         */
        @JsonProperty("resource")
        String resource,

        /**
         * UUID of the resource instance (konversasjonsId)
         */
        @JsonProperty("resourceinstance")
        UUID resourceinstance,

        /**
         * URI source of the event
         */
        @JsonProperty("source")
        URI source,

        /**
         * CloudEvents specification version (default: "1.0")
         */
        @JsonProperty("specversion")
        String specversion,

        /**
         * Event type (e.g., no.altinn.correspondence.correspondencepublished)
         */
        @JsonProperty("type")
        String type,

        /**
         * Subject of the event (optional)
         */
        @JsonProperty("subject")
        String subject,

        /**
         * Alternative subject (e.g., /organisation/889640782)
         */
        @JsonProperty("alternativesubject")
        String alternativesubject,

        /**
         * Timestamp of the event in ISO 8601 format
         */
        @JsonProperty("time")
        OffsetDateTime time
) {
}

