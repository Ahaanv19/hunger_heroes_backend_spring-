package com.open.spring.mvc.mortevision;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter
public class QueueConverter implements AttributeConverter<Queue, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Queue queue) {
        try {
            return objectMapper.writeValueAsString(queue);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting Queue to JSON", e);
        }
    }

    @Override
    public Queue convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Queue.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to Queue", e);
        }
    }
}

