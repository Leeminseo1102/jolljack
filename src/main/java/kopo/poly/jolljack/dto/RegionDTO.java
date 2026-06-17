package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RegionDTO {

    // 지역 아이디 (PK)
    private Long regionId;

    // 시도명 (VARCHAR 50)
    private String sidoName;

    // 시군구명 (VARCHAR 50)
    private String sigunguName;

    // 지역명 전체 (VARCHAR 150)
    private String fullRegionName;

    // 생성일시
    private String createdAt;

    //기상청 지역 아이디
    private String kmaAreaId;

    //기상청 허브 지역 아이디
    private Integer kmaStn;

}