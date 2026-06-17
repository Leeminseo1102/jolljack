package kopo.poly.jolljack.dto;//레코드로 수정

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CropAnalysisDTO {

    // 분석 아이디 (PK)
    private Long analysisId;

    // 회원번호 (FK → user.user_id)
    private Long userId;

    // 지역 아이디 (FK → region.region_id)
    private Long regionId;

    // 추천 작물 아이디 (FK → crop.crop_id)
    private Long recommendedCropId;

    // 추천 이유
    private String recommendedReason;

    // 재배 방법
    private String cultivationMethod;

    // 리스크 요약
    private String riskSummary;

    // 날씨 요약
    private String weatherSummary;

    // 추천 점수
    private Integer score;

    // 생성일시
    private String createdAt;

//    // -----------------------------------------------
//    // DB 컬럼 외 - 조회 편의용 필드 (JOIN 결과)
//    // -----------------------------------------------
//
    // 작물명 (crop 테이블 JOIN)
    private String cropName;

    // 지역명 전체 (region 테이블 JOIN)
    private String fullRegionName;

}