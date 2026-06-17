package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WeatherSumDTO {

    // 지역명 전체
    private String fullRegionName;

    //날씨 요약
    private String weatherSummary;

    // 이건 예외 처리를 위해서 만든ㄱ------

    // 처리 결과 코드
    private String result;

    // 처리 결과 메시지
    private String msg;

    //----------------------------------

    //1년
    // 1년 평균 기온
    private String yearAvgTemp;

    // 1년 최고 기온
    private String yearMaxTemp;

    // 1년 최저 기온
    private String yearMinTemp;

    // 1년 평균 습도
    private String yearHumidity;

    // 1년 누적 강수량
    private String yearRainfall;

    // 1년 평균 풍속
    private String yearWindSpeed;

    // 1년 일조량
    private String yearSunshine;

    //6개월
    // 6개월 평균 기온
    private String sixMonthAvgTemp;

    // 6개월 최고 기온
    private String sixMonthMaxTemp;

    // 6개월 최저 기온
    private String sixMonthMinTemp;

    // 6개월 평균 습도
    private String sixMonthHumidity;

    // 6개월 누적 강수량
    private String sixMonthRainfall;

    // 6개월 평균 풍속
    private String sixMonthWindSpeed;

    // 6개월 일조량
    private String sixMonthSunshine;

    //3개월
    // 3개월 평균 기온
    private String threeMonthAvgTemp;

    // 3개월 최고 기온
    private String threeMonthMaxTemp;

    // 3개월 최저 기온
    private String threeMonthMinTemp;

    // 3개월 평균 습도
    private String threeMonthHumidity;

    // 3개월 누적 강수량
    private String threeMonthRainfall;

    // 3개월 평균 풍속
    private String threeMonthWindSpeed;

    // 3개월 일조량
    private String threeMonthSunshine;


}