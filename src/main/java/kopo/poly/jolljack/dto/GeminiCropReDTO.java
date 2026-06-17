package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GeminiCropReDTO {

    // 추천 작물명
    private String recommendedCropName;

    // 추천 이유
    private String recommendedReason;

    // 재배 방법
    private String cultivationMethod;

    // 리스크 요약
    private String riskSummary;

    // 추천 점수
    private Integer score;

}