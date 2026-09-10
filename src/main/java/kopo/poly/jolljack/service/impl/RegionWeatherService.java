package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.RegionGridDTO;
import kopo.poly.jolljack.dto.RegionWeatherDTO;
import kopo.poly.jolljack.dto.RegionWeatherRE;
import kopo.poly.jolljack.mapper.IWeatherMapper;
import kopo.poly.jolljack.service.IRegionWeatherService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionWeatherService implements IRegionWeatherService {

    private final IWeatherMapper weatherMapper;

    @Value("${kma.daily.api-key}")
    private String apiKey;

    private static final String API_URL =
            "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst";

    private static final int CACHE_MINUTES = 30;

    private static final ZoneId SEOUL_ZONE =
            ZoneId.of("Asia/Seoul");

    private final Map<Long, WeatherCache> weatherCache =
            new ConcurrentHashMap<>();

    @Override
    public RegionWeatherDTO getRegionWeatherProc(Long userId) throws Exception {

        log.info("{}.getRegionWeatherProc Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalStateException("로그인 정보가 존재하지 않습니다.");
        }

        RegionGridDTO pDTO = RegionGridDTO.builder()
                .userId(userId)
                .build();

        Long regionId = weatherMapper.getRegionId(pDTO);

        if (regionId == null) {
            throw new IllegalStateException("사용자의 지역 정보를 찾을 수 없습니다.");
        }

        WeatherCache cache = weatherCache.get(regionId);// 이해 놉

        if (isValidCache(cache)) {

            log.info("Weather Cache Hit! regionId : {}", regionId);

            RegionWeatherDTO weatherDTO = cache.weatherDTO();

            log.info("{}.getRegionWeatherProc End!", this.getClass().getName());

            return weatherDTO;
        }// 이해 놉

        pDTO = RegionGridDTO.builder()
                .regionId(regionId)
                .build();

        RegionGridDTO rDTO = weatherMapper.getRegionGrid(pDTO);

        if (rDTO == null ||
                rDTO.nx() == null ||
                rDTO.ny() == null) {

            throw new IllegalStateException(
                    "지역의 기상청 격자 좌표를 찾을 수 없습니다."
            );
        }

        RegionWeatherRE weatherRE =
                getRegionWeatherAPI(rDTO);

        RegionWeatherDTO weatherDTO =
                makeRegionWeatherDTO(
                        rDTO,
                        weatherRE
                );

        WeatherCache cacheDTO = new WeatherCache(
                weatherDTO,
                LocalDateTime.now(SEOUL_ZONE)
        );

        weatherCache.put(
                regionId,
                cacheDTO
        );

        log.info("Weather Cache Save! regionId : {}", regionId);

        log.info("{}.getRegionWeatherProc End!", this.getClass().getName());

        return weatherDTO;
    }

    private RegionWeatherRE getRegionWeatherAPI(RegionGridDTO pDTO) throws Exception {

        log.info("{}.getRegionWeatherAPI Start!", this.getClass().getName());

        LocalDateTime baseDateTime =
                getBaseDateTime();

        String baseDate = baseDateTime.format(
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        String baseTime = baseDateTime.format(
                DateTimeFormatter.ofPattern("HHmm")
        );

        String url = API_URL
                + "?pageNo=1"
                + "&numOfRows=1000"
                + "&dataType=JSON"
                + "&base_date=" + baseDate
                + "&base_time=" + baseTime
                + "&nx=" + pDTO.nx()
                + "&ny=" + pDTO.ny()
                + "&authKey=" + apiKey;

        log.info("baseDate : {}", baseDate);
        log.info("baseTime : {}", baseTime);
        log.info("nx : {}", pDTO.nx());
        log.info("ny : {}", pDTO.ny());

        RestClient restClient =
                RestClient.create();

        RegionWeatherRE rDTO = restClient.get()
                .uri(url)
                .retrieve()
                .body(RegionWeatherRE.class);

        if (rDTO == null ||
                rDTO.response() == null ||
                rDTO.response().body() == null ||
                rDTO.response().body().items() == null) {

            throw new IllegalStateException(
                    "기상청 날씨 정보를 불러오지 못했습니다."
            );
        }

        log.info("{}.getRegionWeatherAPI End!", this.getClass().getName());

        return rDTO;
    }

    private RegionWeatherDTO makeRegionWeatherDTO(RegionGridDTO gridDTO, RegionWeatherRE weatherRE) {

        log.info("{}.makeRegionWeatherDTO Start!", this.getClass().getName());

        List<RegionWeatherRE.Item> rList =
                weatherRE.response()
                        .body()
                        .items()
                        .item();

        if (rList == null || rList.isEmpty()) {
            throw new IllegalStateException(
                    "기상청 예보 데이터가 존재하지 않습니다."
            );
        }

        LocalDateTime forecastDateTime =
                getNearestForecastDateTime(rList);

        String forecastDate = forecastDateTime.format(
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        String forecastTime = forecastDateTime.format(
                DateTimeFormatter.ofPattern("HHmm")
        );

        Map<String, String> weatherMap =
                new HashMap<>();

        for (RegionWeatherRE.Item item : rList) {

            if (forecastDate.equals(item.fcstDate()) &&
                    forecastTime.equals(item.fcstTime())) {

                weatherMap.put(
                        item.category(),
                        item.fcstValue()
                );
            }
        }

        String temp =
                weatherMap.getOrDefault("TMP", "");

        String humidity =
                weatherMap.getOrDefault("REH", "");

        String rainfall =
                weatherMap.getOrDefault("PCP", "강수없음");

        String rainChance =
                weatherMap.getOrDefault("POP", "0");

        String sky =
                weatherMap.getOrDefault("SKY", "");

        String pty =
                weatherMap.getOrDefault("PTY", "0");

        String weather =
                getWeatherStatus(sky, pty);

        RegionWeatherDTO rDTO = RegionWeatherDTO.builder()
                .region(CmmUtil.nvl(gridDTO.region()))
                .temp(temp)
                .weather(weather)
                .humidity(humidity)
                .rainfall(rainfall)
                .rainChance(rainChance)
                .build();

        log.info("forecastDate : {}", forecastDate);
        log.info("forecastTime : {}", forecastTime);
        log.info("weather : {}", weather);

        log.info("{}.makeRegionWeatherDTO End!", this.getClass().getName());

        return rDTO;
    }

    private LocalDateTime getNearestForecastDateTime(List<RegionWeatherRE.Item> rList) {

        LocalDateTime now =
                LocalDateTime.now(SEOUL_ZONE);

        LocalDateTime rDateTime = rList.stream()
                .map(item -> LocalDateTime.of(
                        LocalDate.parse(
                                item.fcstDate(),
                                DateTimeFormatter.ofPattern("yyyyMMdd")
                        ),
                        LocalTime.parse(
                                item.fcstTime(),
                                DateTimeFormatter.ofPattern("HHmm")
                        )
                ))
                .distinct()
                .min(Comparator.comparingLong(dateTime ->
                        Math.abs(
                                Duration.between(
                                        now,
                                        dateTime
                                ).toMinutes()
                        )
                ))
                .orElseThrow(() ->
                        new IllegalStateException(
                                "현재 시간에 맞는 예보 정보를 찾을 수 없습니다."
                        )
                );

        return rDateTime;
    }

    private LocalDateTime getBaseDateTime() {

        LocalDateTime now =
                LocalDateTime.now(SEOUL_ZONE)
                        .minusMinutes(10);

        int[] baseHours = {
                2, 5, 8, 11,
                14, 17, 20, 23
        };

        for (int i = baseHours.length - 1; i >= 0; i--) {

            if (now.getHour() >= baseHours[i]) {

                LocalDateTime rDateTime = now
                        .withHour(baseHours[i])
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                return rDateTime;
            }
        }

        LocalDateTime rDateTime = now
                .minusDays(1)
                .withHour(23)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return rDateTime;
    }

    private String getWeatherStatus(
            String sky,
            String pty
    ) {

        String weather;

        if ("1".equals(pty) || "4".equals(pty)) {

            weather = "비";

        } else if ("2".equals(pty)) {

            weather = "눈 / 비";

        } else if ("3".equals(pty)) {

            weather = "눈";

        } else {

            weather = switch (sky) {
                case "1" -> "맑음";
                case "3" -> "구름 많음";
                case "4" -> "흐림";
                default -> "알 수 없음";
            };
        }

        return weather;
    }

    private boolean isValidCache(
            WeatherCache cache
    ) {

        if (cache == null) {
            return false;
        }

        LocalDateTime expireTime =
                cache.cachedAt()
                        .plusMinutes(CACHE_MINUTES);

        boolean result = expireTime.isAfter(
                LocalDateTime.now(SEOUL_ZONE)
        );

        return result;
    }

    private record WeatherCache(
            RegionWeatherDTO weatherDTO,
            LocalDateTime cachedAt
    ) {
    }
}