package com.haconaka.demo.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;

@Converter
public class LongListJsonConverter implements AttributeConverter<List<Long>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || dbData.equals("null")) {
            return new ArrayList<>(); // 가변 리스트로 반환하는 것이 이후 조작에 편합니다.
        }
        try {
            // ✅ 여기서 Long.class를 String.class로 바꿔야 합니다!
            return objectMapper.readValue(dbData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}