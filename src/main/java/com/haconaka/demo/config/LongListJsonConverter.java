package com.haconaka.demo.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@Converter
public class LongListJsonConverter implements AttributeConverter<List<Long>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        // 💡 자바 객체가 null이면 DB에도 null(또는 빈 배열)을 넣도록 설정
        if (attribute == null || attribute.isEmpty()) {
            return "[]"; // 또는 null; (DB 설계에 따라 선택)
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        // 💡 DB 값이 null이거나 비어있으면 빈 리스트 반환
        if (dbData == null || dbData.isBlank() || dbData.equals("null")) {
            return List.of();
        }
        try {
            // 단순 List.class 보다는 정확한 타입을 지정하는 것이 안전합니다.
            return objectMapper.readValue(dbData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}