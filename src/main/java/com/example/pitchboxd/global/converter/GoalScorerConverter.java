package com.example.pitchboxd.global.converter;

import com.example.pitchboxd.match.domain.GoalScorer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class GoalScorerConverter implements AttributeConverter<List<GoalScorer>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<GoalScorer> attribute) {
        // null이거나 비어있으면 DB에 null 또는 빈 배열 저장
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("득점자 객체를 JSON 문자열로 변환하는 데 실패했습니다. 데이터: " + attribute, e);
        }
    }

    @Override
    public List<GoalScorer> convertToEntityAttribute(String dbData) {
        // 2. 방어적 코드: null, 빈 문자열, 혹은 문자열 "null"이 들어오는 경우 처리
        if (dbData == null || dbData.isBlank() || dbData.equals("null")) {
            return new ArrayList<>();
        }

        try {
            // 3. 디버깅 팁: 에러 발생 시 실제 dbData가 뭔지 로그로 찍어보세요.
            return objectMapper.readValue(dbData, new TypeReference<List<GoalScorer>>() {
            });
        } catch (JsonProcessingException e) {
            // 실제 어떤 데이터 때문에 터졌는지 에러 메시지에 포함 (실무 필수)
            throw new IllegalArgumentException("JSON 데이터를 리스트로 변환할 수 없습니다. 원인 데이터: [" + dbData + "]", e);
        }
    }
}
