package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NonglimDTO {

    // 작물명 (사과, 감자 등)
    private String cropName;

    // 정제된 재배 정보 (Gemini에 넣을 값)
    private String cultivationText;
}