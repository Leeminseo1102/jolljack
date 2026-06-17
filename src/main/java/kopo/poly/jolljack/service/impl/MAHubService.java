package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.WeatherSumDTO;
import kopo.poly.jolljack.service.IMAHubService;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MAHubService implements IMAHubService {

    @Value("${kma.daily.api-key}")
    private String apiKey;

    private static final String API_URL =
            "https://apihub.kma.go.kr/api/typ01/url/arcltr_sfc_day.php";

    private static final String RESULT_OK = "HUB_OK";
    private static final String RESULT_STN_EMPTY = "HUB_STN_EMPTY";
    private static final String RESULT_NO_DATA = "HUB_NO_DATA";
    private static final String RESULT_API_FAIL = "HUB_API_FAIL";
    private static final String RESULT_PARSE_FAIL = "HUB_PARSE_FAIL";

    @Override
    public WeatherSumDTO getDailyWeather(RegionDTO pDTO) throws Exception {

        log.info("{}.getDailyWeather Start!", this.getClass().getName());

        try {
            Integer kmaStn = pDTO.getKmaStn();

            log.info("region : {}", CmmUtil.nvl(pDTO.getFullRegionName()));
            log.info("kmaStn : {}", kmaStn);

            if (kmaStn == null || kmaStn <= 0) {
                log.info("{}.getDailyWeather HUB_STN_EMPTY", this.getClass().getName());

                return WeatherSumDTO.builder()
                        .result(RESULT_STN_EMPTY)
                        .msg("기상청 허브 관측소 코드가 없습니다.")
                        .build();
            }

            WeatherPeriod yearPeriod = getPeriodWeather(kmaStn, 12);
            WeatherPeriod sixMonthPeriod = getPeriodWeather(kmaStn, 6);
            WeatherPeriod threeMonthPeriod = getPeriodWeather(kmaStn, 3);

            if (yearPeriod == null && sixMonthPeriod == null && threeMonthPeriod == null) {
                log.info("{}.getDailyWeather HUB_NO_DATA", this.getClass().getName());

                return WeatherSumDTO.builder()
                        .result(RESULT_NO_DATA)
                        .msg("기상청 허브 일별 데이터가 없습니다.")
                        .build();
            }

            String weatherSummary = buildWeatherSummary(yearPeriod, sixMonthPeriod, threeMonthPeriod);

            return WeatherSumDTO.builder()
                    .fullRegionName(pDTO.getFullRegionName())
                    .weatherSummary(weatherSummary)
                    .result(RESULT_OK)
                    .msg("허브 날씨 데이터 조회 성공")

                    // 1년
                    .yearAvgTemp(yearPeriod != null ? yearPeriod.avgTemp : "")
                    .yearMaxTemp(yearPeriod != null ? yearPeriod.maxTemp : "")
                    .yearMinTemp(yearPeriod != null ? yearPeriod.minTemp : "")
                    .yearRainfall(yearPeriod != null ? yearPeriod.rainfall : "")
                    .yearWindSpeed(yearPeriod != null ? yearPeriod.windSpeed : "")

                    // 6개월
                    .sixMonthAvgTemp(sixMonthPeriod != null ? sixMonthPeriod.avgTemp : "")
                    .sixMonthMaxTemp(sixMonthPeriod != null ? sixMonthPeriod.maxTemp : "")
                    .sixMonthMinTemp(sixMonthPeriod != null ? sixMonthPeriod.minTemp : "")
                    .sixMonthRainfall(sixMonthPeriod != null ? sixMonthPeriod.rainfall : "")
                    .sixMonthWindSpeed(sixMonthPeriod != null ? sixMonthPeriod.windSpeed : "")

                    // 3개월
                    .threeMonthAvgTemp(threeMonthPeriod != null ? threeMonthPeriod.avgTemp : "")
                    .threeMonthMaxTemp(threeMonthPeriod != null ? threeMonthPeriod.maxTemp : "")
                    .threeMonthMinTemp(threeMonthPeriod != null ? threeMonthPeriod.minTemp : "")
                    .threeMonthRainfall(threeMonthPeriod != null ? threeMonthPeriod.rainfall : "")
                    .threeMonthWindSpeed(threeMonthPeriod != null ? threeMonthPeriod.windSpeed : "")
                    .build();

        } catch (Exception e) {
            log.info("{}.getDailyWeather Error : {}", this.getClass().getName(), e.getMessage(), e);

            return WeatherSumDTO.builder()
                    .result(RESULT_PARSE_FAIL)
                    .msg("허브 데이터 처리 중 오류 발생")
                    .build();
        }
    }

    private WeatherPeriod getPeriodWeather(Integer kmaStn, int months) {

        String tm1 = getStartDate(months);
        String tm2 = getEndDate();

        String url = API_URL
                + "?authKey=" + apiKey
                + "&stn=" + kmaStn
                + "&tm1=" + tm1
                + "&tm2=" + tm2
                + "&help=0"
                + "&disp=1";

        log.info("request url({}개월) : {}", months, url);

        RestClient restClient = RestClient.create();

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);


        if (CmmUtil.nvl(response).isEmpty()) {
            log.info("{}.getPeriodWeather HUB_API_FAIL - empty response", this.getClass().getName());
            return null;
        }

        String[] lines = response.split("\\r?\\n");

        double sumAvgTemp = 0D;
        double sumMaxTemp = 0D;
        double sumMinTemp = 0D;
        double sumRainfall = 0D;
        double sumWindSpeed = 0D;

        int avgTempCount = 0;
        int maxTempCount = 0;
        int minTempCount = 0;
        int windSpeedCount = 0;
        int validCount = 0;

        for (String line : lines) {

            String row = CmmUtil.nvl(line).trim();

            if (row.isEmpty()) {
                continue;
            }

            if (row.startsWith("#")) {
                continue;
            }

            String[] cols = row.split(",");

            if (cols.length < 15) {
                log.info("skip - cols length 부족 : {}", cols.length);
                continue;
            }

            double avgTemp = parseDouble(cols[5]);
            double maxTemp = parseDouble(cols[6]);
            double minTemp = parseDouble(cols[8]);
            double rainfall = parseDouble(cols[10]);
            double windSpeed = parseDouble(cols[14]);

            if (isInvalidValue(avgTemp) && isInvalidValue(maxTemp) && isInvalidValue(minTemp)) {
                continue;
            }

            if (!isInvalidValue(avgTemp)) {
                sumAvgTemp += avgTemp;
                avgTempCount++;
            }

            if (!isInvalidValue(maxTemp)) {
                sumMaxTemp += maxTemp;
                maxTempCount++;
            }

            if (!isInvalidValue(minTemp)) {
                sumMinTemp += minTemp;
                minTempCount++;
            }

            if (!isInvalidValue(rainfall)) {
                sumRainfall += rainfall;
            }

            if (!isInvalidValue(windSpeed)) {
                sumWindSpeed += windSpeed;
                windSpeedCount++;
            }

            validCount++;
        }

        log.info("months : {}", months);
        log.info("validCount : {}", validCount);
        log.info("avgTempCount : {}", avgTempCount);
        log.info("maxTempCount : {}", maxTempCount);
        log.info("minTempCount : {}", minTempCount);
        log.info("windSpeedCount : {}", windSpeedCount);

        if (validCount == 0) {
            return null;
        }

        return WeatherPeriod.builder()
                .avgTemp(avgTempCount > 0 ? formatDecimal(sumAvgTemp / avgTempCount) : "")
                .maxTemp(maxTempCount > 0 ? formatDecimal(sumMaxTemp / maxTempCount) : "")
                .minTemp(minTempCount > 0 ? formatDecimal(sumMinTemp / minTempCount) : "")
                .rainfall(formatDecimal(sumRainfall))
                .windSpeed(windSpeedCount > 0 ? formatDecimal(sumWindSpeed / windSpeedCount) : "")
                .build();
    }

    /**
     * 완료된 최근 N개월 시작일
     * 예: 오늘이 2026-04-14이고 months=3이면 20260101 반환
     */
    private String getStartDate(int months) {

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate startDate = now.withDayOfMonth(1).minusMonths(months);

        return startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 완료된 최근 종료일
     * 예: 오늘이 2026-04-14면 20260331 반환
     */
    private String getEndDate() {

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate endDate = now.withDayOfMonth(1).minusDays(1);

        return endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 문자열 숫자 안전 변환
     */
    private double parseDouble(String value) {

        String safeValue = CmmUtil.nvl(value).trim();

        if (safeValue.isEmpty()) {
            return -999D;
        }

        try {
            return Double.parseDouble(safeValue);
        } catch (Exception e) {
            return -999D;
        }
    }

    /**
     * 결측치 여부 확인
     */
    private boolean isInvalidValue(double value) {
        return value == -999D;
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
            sb.append("최근 1년 일별 관측 평균 기준 평균기온 ")
                    .append(yearPeriod.avgTemp).append("℃, 일최고기온 평균 ")
                    .append(yearPeriod.maxTemp).append("℃, 일최저기온 평균 ")
                    .append(yearPeriod.minTemp).append("℃, 누적강수량 ")
                    .append(yearPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(yearPeriod.windSpeed).append("m/s 입니다. ");
        }

        if (sixMonthPeriod != null) {
            sb.append("최근 6개월 일별 관측 평균 기준 평균기온 ")
                    .append(sixMonthPeriod.avgTemp).append("℃, 일최고기온 평균 ")
                    .append(sixMonthPeriod.maxTemp).append("℃, 일최저기온 평균 ")
                    .append(sixMonthPeriod.minTemp).append("℃, 누적강수량 ")
                    .append(sixMonthPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(sixMonthPeriod.windSpeed).append("m/s 입니다. ");
        }

        if (threeMonthPeriod != null) {
            sb.append("최근 3개월 일별 관측 평균 기준 평균기온 ")
                    .append(threeMonthPeriod.avgTemp).append("℃, 일최고기온 평균 ")
                    .append(threeMonthPeriod.maxTemp).append("℃, 일최저기온 평균 ")
                    .append(threeMonthPeriod.minTemp).append("℃, 누적강수량 ")
                    .append(threeMonthPeriod.rainfall).append("mm, 평균풍속 ")
                    .append(threeMonthPeriod.windSpeed).append("m/s 입니다.");
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
        private String rainfall;
        private String windSpeed;
    }
}