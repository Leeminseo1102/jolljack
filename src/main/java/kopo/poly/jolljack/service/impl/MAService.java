package kopo.poly.jolljack.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.WeatherSumDTO;
import kopo.poly.jolljack.service.IMAService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MAService implements IMAService {

    @Value("${public.data.api-key}")
    private String apiKey;

    private static final String API_URL =
            "http://apis.data.go.kr/1360000/FmlandWthrInfoService/getMmStatistics";

    private static final String RESULT_OK = "MA_OK";
    private static final String RESULT_AREA_ID_EMPTY = "AREA_ID_EMPTY";
    private static final String RESULT_NO_DATA = "MA_NO_DATA";
    private static final String RESULT_PARSE_FAIL = "MA_PARSE_FAIL";

    @Override
    public WeatherSumDTO getMonthWeather(RegionDTO pDTO) throws Exception {

        log.info("{}.getMonthWeather Start!", this.getClass().getName());

        try {
            String areaId = CmmUtil.nvl(pDTO.getKmaAreaId());

            if (areaId.isEmpty()) {
                log.info("{}.getMonthWeather AREA_ID_EMPTY", this.getClass().getName());

                return WeatherSumDTO.builder()
                        .result(RESULT_AREA_ID_EMPTY)
                        .msg("기상청 지역 코드가 없어 기상청 허브 API로 전환합니다.")
                        .build();
            }

            WeatherPeriod yearPeriod = getPeriodWeather(areaId, 12);
            WeatherPeriod sixMonthPeriod = getPeriodWeather(areaId, 6);
            WeatherPeriod threeMonthPeriod = getPeriodWeather(areaId, 3);

            if (yearPeriod == null && sixMonthPeriod == null && threeMonthPeriod == null) {
                log.info("{}.getMonthWeather MA_NO_DATA", this.getClass().getName());

                return WeatherSumDTO.builder()
                        .result(RESULT_NO_DATA)
                        .msg("기상청 월통계 데이터가 없습니다.")
                        .build();
            }

            String weatherSummary = buildWeatherSummary(yearPeriod, sixMonthPeriod, threeMonthPeriod);

            WeatherSumDTO rDTO = WeatherSumDTO.builder()
                    .fullRegionName(CmmUtil.nvl(pDTO.getFullRegionName()))
                    .weatherSummary(weatherSummary)
                    .result(RESULT_OK)
                    .msg("기상청 월통계 조회 성공")

                    // 1년
                    .yearAvgTemp(yearPeriod != null ? yearPeriod.avgTemp : "")
                    .yearMaxTemp(yearPeriod != null ? yearPeriod.maxTemp : "")
                    .yearMinTemp(yearPeriod != null ? yearPeriod.minTemp : "")
                    .yearHumidity(yearPeriod != null ? yearPeriod.humidity : "")
                    .yearRainfall(yearPeriod != null ? yearPeriod.rainfall : "")
                    .yearWindSpeed(yearPeriod != null ? yearPeriod.windSpeed : "")
                    .yearSunshine(yearPeriod != null ? yearPeriod.sunshine : "")

                    // 6개월
                    .sixMonthAvgTemp(sixMonthPeriod != null ? sixMonthPeriod.avgTemp : "")
                    .sixMonthMaxTemp(sixMonthPeriod != null ? sixMonthPeriod.maxTemp : "")
                    .sixMonthMinTemp(sixMonthPeriod != null ? sixMonthPeriod.minTemp : "")
                    .sixMonthHumidity(sixMonthPeriod != null ? sixMonthPeriod.humidity : "")
                    .sixMonthRainfall(sixMonthPeriod != null ? sixMonthPeriod.rainfall : "")
                    .sixMonthWindSpeed(sixMonthPeriod != null ? sixMonthPeriod.windSpeed : "")
                    .sixMonthSunshine(sixMonthPeriod != null ? sixMonthPeriod.sunshine : "")

                    // 3개월
                    .threeMonthAvgTemp(threeMonthPeriod != null ? threeMonthPeriod.avgTemp : "")
                    .threeMonthMaxTemp(threeMonthPeriod != null ? threeMonthPeriod.maxTemp : "")
                    .threeMonthMinTemp(threeMonthPeriod != null ? threeMonthPeriod.minTemp : "")
                    .threeMonthHumidity(threeMonthPeriod != null ? threeMonthPeriod.humidity : "")
                    .threeMonthRainfall(threeMonthPeriod != null ? threeMonthPeriod.rainfall : "")
                    .threeMonthWindSpeed(threeMonthPeriod != null ? threeMonthPeriod.windSpeed : "")
                    .threeMonthSunshine(threeMonthPeriod != null ? threeMonthPeriod.sunshine : "")
                    .build();

            log.info("{}.getMonthWeather End!", this.getClass().getName());

            return rDTO;

        } catch (Exception e) {
            log.info("{}.getMonthWeather Error : {}", this.getClass().getName(), e.getMessage(), e);

            return WeatherSumDTO.builder()
                    .result(RESULT_PARSE_FAIL)
                    .msg("기상청 월통계 데이터 처리 중 오류가 발생했습니다.")
                    .build();
        }
    }

    private WeatherPeriod getPeriodWeather(String areaId, int months) throws Exception {

        String stYm = getStartYm(months);
        String edYm = getEndYm();

        String url = API_URL
                + "?serviceKey=" + apiKey
                + "&numOfRows=20"
                + "&pageNo=1"
                + "&dataType=JSON"
                + "&ST_YM=" + stYm
                + "&ED_YM=" + edYm
                + "&AREA_ID=" + areaId
                + "&PA_CROP_SPE_ID=PA999999";

        log.info("request url({}개월) : {}", months, url);

        RestClient restClient = RestClient.create();

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        if (CmmUtil.nvl(response).isEmpty()) {
            log.info("{}.getPeriodWeather MA_API_FAIL - empty response", this.getClass().getName());
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response);

        JsonNode headerNode = rootNode.path("response").path("header");
        String resultCode = CmmUtil.nvl(headerNode.path("resultCode").asText());
        String resultMsg = CmmUtil.nvl(headerNode.path("resultMsg").asText());

        if (!"00".equals(resultCode) && !"0".equals(resultCode)) {
            log.info("{}.getPeriodWeather MA_API_FAIL - resultCode : {}, resultMsg : {}",
                    this.getClass().getName(), resultCode, resultMsg);
            return null;
        }

        JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

        List<JsonNode> itemList = getItemList(itemNode);

        if (itemList.isEmpty()) {
            log.info("{}.getPeriodWeather MA_NO_DATA - months : {}", this.getClass().getName(), months);
            return null;
        }

        double sumAvgTemp = 0D;
        double sumMaxTemp = 0D;
        double sumMinTemp = 0D;
        double sumHumidity = 0D;
        double sumWindSpeed = 0D;
        double sumRainfall = 0D;
        double sumSunshine = 0D;

        int count = itemList.size();

        for (JsonNode item : itemList) {
            sumAvgTemp += getDoubleValue(item, "mnhAvgTa");
            sumMaxTemp += getDoubleValue(item, "mnhMaxTa");
            sumMinTemp += getDoubleValue(item, "mnhMinTa");
            sumHumidity += getDoubleValue(item, "mnhAvgRhm");
            sumWindSpeed += getDoubleValue(item, "mnhAvgWs");
            sumRainfall += getDoubleValue(item, "mnhSumRn");
            sumSunshine += getDoubleValue(item, "mnhSumSs");
        }

        return WeatherPeriod.builder()
                .avgTemp(formatDecimal(sumAvgTemp / count))
                .maxTemp(formatDecimal(sumMaxTemp / count))
                .minTemp(formatDecimal(sumMinTemp / count))
                .humidity(formatDecimal(sumHumidity / count))
                .windSpeed(formatDecimal(sumWindSpeed / count))
                .rainfall(formatDecimal(sumRainfall))
                .sunshine(formatDecimal(sumSunshine))
                .build();
    }

    /**
     * 완료된 최근 N개월 시작 연월
     * 예: 오늘이 2026-04-14이고 months=6이면 202510 반환
     */
    private String getStartYm(int months) {

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        YearMonth endMonth = YearMonth.from(now).minusMonths(1);
        YearMonth startMonth = endMonth.minusMonths(months - 1);

        return startMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * 완료된 최근 종료 연월
     * 예: 오늘이 2026-04-14면 202603 반환
     */
    private String getEndYm() {

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        YearMonth endMonth = YearMonth.from(now).minusMonths(1);

        return endMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * item 노드를 List 형태로 변환
     * item이 배열일 수도 있고 단건 객체일 수도 있으므로 둘 다 처리
     */
    private List<JsonNode> getItemList(JsonNode itemNode) {

        List<JsonNode> rList = new ArrayList<>();

        if (itemNode == null || itemNode.isMissingNode() || itemNode.isNull()) {
            return rList;
        }

        if (itemNode.isArray()) {
            for (JsonNode item : itemNode) {
                rList.add(item);
            }
            return rList;
        }

        if (itemNode.isObject()) {
            rList.add(itemNode);
        }

        return rList;
    }

    /**
     * JSON 숫자값 안전 추출
     */
    private double getDoubleValue(JsonNode node, String fieldName) {

        String value = CmmUtil.nvl(node.path(fieldName).asText()).trim();

        if (value.isEmpty()) {
            return 0D;
        }

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0D;
        }
    }

    /**
     * 소수점 1자리 문자열 변환
     */
    private String formatDecimal(double value) {
        return String.format("%.1f", value);
    }

    /**
     * 날씨 요약 문장 생성
     */
    private String buildWeatherSummary(WeatherPeriod yearPeriod,
                                       WeatherPeriod sixMonthPeriod,
                                       WeatherPeriod threeMonthPeriod) {

        StringBuilder sb = new StringBuilder();

        if (yearPeriod != null) {
            sb.append("최근 1년 기준 평균기온 ")
                    .append(yearPeriod.avgTemp).append("℃, 최고기온 ")
                    .append(yearPeriod.maxTemp).append("℃, 최저기온 ")
                    .append(yearPeriod.minTemp).append("℃, 평균습도 ")
                    .append(yearPeriod.humidity).append("%, 누적강수량 ")
                    .append(yearPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(yearPeriod.windSpeed).append("m/s, 누적일조시간 ")
                    .append(yearPeriod.sunshine).append("시간입니다. ");
        }

        if (sixMonthPeriod != null) {
            sb.append("최근 6개월 기준 평균기온 ")
                    .append(sixMonthPeriod.avgTemp).append("℃, 최고기온 ")
                    .append(sixMonthPeriod.maxTemp).append("℃, 최저기온 ")
                    .append(sixMonthPeriod.minTemp).append("℃, 평균습도 ")
                    .append(sixMonthPeriod.humidity).append("%, 누적강수량 ")
                    .append(sixMonthPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(sixMonthPeriod.windSpeed).append("m/s, 누적일조시간 ")
                    .append(sixMonthPeriod.sunshine).append("시간입니다. ");
        }

        if (threeMonthPeriod != null) {
            sb.append("최근 3개월 기준 평균기온 ")
                    .append(threeMonthPeriod.avgTemp).append("℃, 최고기온 ")
                    .append(threeMonthPeriod.maxTemp).append("℃, 최저기온 ")
                    .append(threeMonthPeriod.minTemp).append("℃, 평균습도 ")
                    .append(threeMonthPeriod.humidity).append("%, 누적강수량 ")
                    .append(threeMonthPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(threeMonthPeriod.windSpeed).append("m/s, 누적일조시간 ")
                    .append(threeMonthPeriod.sunshine).append("시간입니다.");
        }

        return sb.toString().trim();
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class WeatherPeriod {
        private String avgTemp;
        private String maxTemp;
        private String minTemp;
        private String humidity;
        private String rainfall;
        private String windSpeed;
        private String sunshine;
    }
}