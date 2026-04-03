package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DiseaseDiagnosisDTO {

    // 진단 아이디 (PK)
    private Long diagnosisId;

    // 회원번호 (FK → user.user_id)
    private Long userId;

    // 이미지 경로 (VARCHAR 500)
    private String imageUrl;

    // 원본 파일명 (VARCHAR 255)
    private String originalFileName;

    // 예상 병명 (VARCHAR 150)
    private String predictedDiseaseName;

    // 병설명
    private String diseaseDescription;

    // 해결방안
    private String solutionText;

    // 증상요약 (VARCHAR 20)
    private String symptomSummary;

    // 예방방법
    private String preventionText;

    // 생성일시
    private String createdAt;

//    // -----------------------------------------------
//    // DB 컬럼 외 - 비즈니스 로직용 필드
//    // -----------------------------------------------
//
    // 업로드된 이미지 파일 (MultipartFile → Base64 변환 전 임시 저장)
    // Controller에서 Service로 넘길 때 사용
    private String imageBase64;

    // 이미지 MIME 타입 (ex: image/jpeg, image/png)
    private String imageMimeType;

}