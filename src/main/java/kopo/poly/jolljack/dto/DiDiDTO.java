package kopo.poly.jolljack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiDiDTO(

        // 최종 예측 병충해명
        String predictedDiseaseName,

        // 병충해 설명
        String diseaseDescription,

        // 방제 / 해결 방법
        String solutionText,

        // 증상 요약 + 후보별 매치 점수 요약
        String symptomSummary,

        // 예방 방법
        String preventionText

) {
}