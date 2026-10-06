package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmDicSearchDTO{

    String wordNo;

    String langCode;

    String langNm;

    String wordNm;

    String wordType;
}