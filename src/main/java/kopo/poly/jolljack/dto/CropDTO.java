package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CropDTO {

    // 작물 아이디 (PK)
    private Long cropId;

    // 작물명 (VARCHAR 100)
    private String cropName;

    // 생성일시
    private String createdAt;

}