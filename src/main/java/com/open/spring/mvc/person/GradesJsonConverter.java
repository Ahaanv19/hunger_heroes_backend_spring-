package com.open.spring.mvc.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = false)
public class GradesJsonConverter implements AttributeConverter<List<Map<String, Object>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
        try {
            if (attribute == null) return null;
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert grades to JSON", e);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) return new ArrayList<>();
            return MAPPER.readValue(dbData, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse grades JSON", e);
        }
    }
}
