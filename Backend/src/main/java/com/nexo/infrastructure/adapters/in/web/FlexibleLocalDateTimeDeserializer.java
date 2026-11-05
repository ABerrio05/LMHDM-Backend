package com.nexo.infrastructure.adapters.in.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * HTML date inputs emit yyyy-MM-dd while the domain stores a LocalDateTime.
 * Accept both forms so a task form never fails just because it has no time picker.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value).atTime(23, 59);
            } catch (DateTimeParseException invalidDate) {
                return (LocalDateTime) context.handleWeirdStringValue(
                        LocalDateTime.class, value, "La fecha debe tener formato AAAA-MM-DD o AAAA-MM-DDTHH:mm");
            }
        }
    }
}
