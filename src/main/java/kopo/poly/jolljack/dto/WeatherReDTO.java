package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WeatherReDTO {

    // 지역 아이디
    private Long regionId;

    // 지역명 전체
    private String fullRegionName;

    // 기상청 지역 코드
    private String kmaAreaId;

    // 기상청 관측소 코드
    private Integer kmaStn;

}
