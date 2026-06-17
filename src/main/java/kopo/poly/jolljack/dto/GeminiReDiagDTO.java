package kopo.poly.jolljack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiReDiagDTO(

        // 피해가 관찰되는 부위
        List<String> affectedParts,

        // 이미지에서 보이는 증상 키워드
        List<String> symptomKeywords,

        // 증상 부위에서 보이는 색상
        List<String> colorKeywords,

        // 병반, 구멍, 피해 흔적의 형태
        List<String> shapeKeywords,

        // 피해 유형
        List<String> damageTypes,

        // 해충 관련 시각 흔적

        List<String> pestSigns,

        // 해충이 남긴 식흔/섭식 패턴
        List<String> feedingPatterns,

        // 병해 관련 시각 흔적
        List<String> diseaseSigns,

        // 병반 또는 변색이 퍼진 방식
        List<String> spreadPatterns,

        // 이미지에서 보이는 특징 요약
        String visualSummary

) {
}