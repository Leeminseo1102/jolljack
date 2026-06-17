package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DiseaseDiagnosisDTO {

    // 진단 아이디 PK
    private Long diagnosisId;

    // 회원번호 FK
    private Long userId;

    // 최종 예측 병충해명
    private String predictedDiseaseName;

    // 병충해 설명
    private String diseaseDescription;

    // 방제 / 해결 방법
    private String solutionText;

    // 증상 요약 + 후보별 매치 점수 요약
    private String symptomSummary;

    // 예방 방법
    private String preventionText;

    // 생성일시
    private String createdAt;

}