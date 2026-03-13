package com.open.spring.mvc.bathroom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TimeInOutPairsConverter implements AttributeConverter<List<LocalDateTime[]>, String> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Convert List<LocalDateTime[]> to String (DB format)
    @Override
    public String convertToDatabaseColumn(List<LocalDateTime[]> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";

        return attribute.stream()
                .map(pair -> pair[0].format(formatter) + "--" + pair[1].format(formatter)) // Ensure correct format
                .collect(Collectors.joining(","));  // Join pairs with ","
    }

    // Convert String (DB format) to List<LocalDateTime[]>
    @Override
    public List<LocalDateTime[]> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) return List.of(); // Ensure non-null empty list

        return Arrays.stream(dbData.split(","))
                .map(pair -> {
                    String[] times = pair.split("--");
                    if (times.length != 2) {
                        throw new IllegalArgumentException("Invalid time pair format: " + pair);
                    }
                    return new LocalDateTime[]{
                            LocalDateTime.parse(times[0].trim(), formatter),
                            LocalDateTime.parse(times[1].trim(), formatter)
                    };
                })
                .collect(Collectors.toList());
    }
}
