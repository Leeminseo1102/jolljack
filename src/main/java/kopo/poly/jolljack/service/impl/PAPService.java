package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.GeminiReDiagDTO;
import kopo.poly.jolljack.dto.PapCandidateDTO;
import kopo.poly.jolljack.service.IPAPService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PAPService implements IPAPService {

    private final RestClient restClient;

    @Value("${public.data.api-key}")
    private String publicDataApiKey;

    private static final String PAP_API_HOST = "https://api.odcloud.kr";

    private static final String PAP_API_PATH =
            "/api/15122573/v1/uddi:b1e59884-3b42-490c-9b07-e0f7cda14f60";

    private static final int PER_PAGE = 100;

    @Override
    public List<PapCandidateDTO> getTop3CandidatesProc(GeminiReDiagDTO pDTO) throws Exception {

        log.info("{}.getTop3CandidatesProc Start!", this.getClass().getName());

        if (pDTO == null) {
            throw new IllegalArgumentException("Gemini 1차 특징 추출 결과가 없습니다.");
        }

        List<Map<String, Object>> apiList = getPapApiList();

        if (apiList == null || apiList.isEmpty()) {
            throw new Exception("병충해 API 데이터가 없습니다.");
        }

        List<PapCandidateDTO> rList = apiList.stream()
                .filter(this::isUsableData)
                .map(apiMap -> makeCandidateDTO(pDTO, apiMap))
                .sorted(Comparator.comparing(PapCandidateDTO::score).reversed())
                .limit(3)
                .toList();

        if (rList.size() < 3) {
            throw new Exception("병충해 후보 데이터가 3개 미만입니다.");
        }

        log.info("Top3 PAP candidate count : {}", rList.size());
        log.info("{}.getTop3CandidatesProc End!", this.getClass().getName());

        return rList;
    }

    private List<Map<String, Object>> getPapApiList() throws Exception {

        log.info("{}.getPapApiList Start!", this.getClass().getName());

        try {
            String requestUrl = UriComponentsBuilder
                    .fromHttpUrl(PAP_API_HOST)
                    .path(PAP_API_PATH)
                    .queryParam("page", 1)
                    .queryParam("perPage", PER_PAGE)
                    .queryParam("returnType", "json")
                    .queryParam("serviceKey", publicDataApiKey)
                    .build(false)
                    .toUriString();

            log.info("PAP API Request URL : {}", requestUrl);

            Map<String, Object> responseMap = restClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (responseMap == null || responseMap.get("data") == null) {
                throw new Exception("병충해 API 응답 데이터가 비어 있습니다.");
            }

            Object dataObj = responseMap.get("data");

            if (!(dataObj instanceof List<?> dataList)) {
                throw new Exception("병충해 API data 형식이 올바르지 않습니다.");
            }

            List<Map<String, Object>> rList = new ArrayList<>();

            for (Object obj : dataList) {

                if (obj instanceof Map<?, ?> map) {

                    Map<String, Object> rowMap = new HashMap<>();

                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        rowMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    }

                    rList.add(rowMap);
                }
            }

            log.info("PAP API data count : {}", rList.size());
            log.info("{}.getPapApiList End!", this.getClass().getName());

            return rList;

        } catch (Exception e) {
            log.info("{}.getPapApiList Error : {}", this.getClass().getName(), e.getMessage(), e);
            throw new Exception("병충해 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    private boolean isUsableData(Map<String, Object> apiMap) {

        String deleteYn = getStringValue(apiMap, "삭제여부");

        return !"Y".equalsIgnoreCase(deleteYn);
    }

    private PapCandidateDTO makeCandidateDTO(GeminiReDiagDTO pDTO, Map<String, Object> apiMap) {

        String diseaseName = getStringValue(apiMap, "병충해명");
        String feature = getStringValue(apiMap, "특징");
        String occurrenceContent = getStringValue(apiMap, "발생시기내용");
        String controlContent = getStringValue(apiMap, "방제내용");

        List<String> matchedKeywords = new ArrayList<>();

        String targetText = normalizeWithSynonym(
                diseaseName + " "
                        + feature + " "
                        + occurrenceContent + " "
                        + controlContent
        );

        int score = 0;

        score += getKeywordScore(pDTO.affectedParts(), targetText, 10, matchedKeywords);
        score += getKeywordScore(pDTO.symptomKeywords(), targetText, 25, matchedKeywords);
        score += getKeywordScore(pDTO.damageTypes(), targetText, 15, matchedKeywords);
        score += getKeywordScore(pDTO.colorKeywords(), targetText, 10, matchedKeywords);
        score += getKeywordScore(pDTO.shapeKeywords(), targetText, 10, matchedKeywords);
        score += getKeywordScore(pDTO.diseaseSigns(), targetText, 10, matchedKeywords);
        score += getKeywordScore(pDTO.pestSigns(), targetText, 10, matchedKeywords);
        score += getKeywordScore(pDTO.feedingPatterns(), targetText, 5, matchedKeywords);
        score += getKeywordScore(pDTO.spreadPatterns(), targetText, 5, matchedKeywords);

        score = Math.min(score, 100);

        return PapCandidateDTO.builder()
                .diseaseName(diseaseName)
                .feature(feature)
                .occurrenceContent(occurrenceContent)
                .controlContent(controlContent)
                .score(score)
                .matchedKeywords(matchedKeywords.stream().distinct().toList())
                .build();
    }

    private int getKeywordScore(
            List<String> keywords,
            String targetText,
            int maxScore,
            List<String> matchedKeywords
    ) {

        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }

        List<String> validKeywords = keywords.stream()
                .filter(keyword -> !isInvalidKeyword(keyword))
                .toList();

        if (validKeywords.isEmpty()) {
            return 0;
        }

        int matchCount = 0;

        for (String keyword : validKeywords) {

            String normalizedKeyword = normalizeWithSynonym(keyword);

            if (normalizedKeyword.isEmpty()) {
                continue;
            }

            if (targetText.contains(normalizedKeyword)) {
                matchCount++;
                matchedKeywords.add(keyword);
                continue;
            }

            if (isPartialMatched(normalizedKeyword, targetText)) {
                matchCount++;
                matchedKeywords.add(keyword);
            }
        }

        if (matchCount == 0) {
            return 0;
        }

        return Math.min(maxScore, (maxScore * matchCount) / validKeywords.size());
    }

    private boolean isPartialMatched(String keyword, String targetText) {

        if (keyword.length() < 4) {
            return false;
        }

        List<String> tokenList = splitKeyword(keyword);

        for (String token : tokenList) {

            if (token.length() < 2) {
                continue;
            }

            if (targetText.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInvalidKeyword(String keyword) {

        String value = CmmUtil.nvl(keyword);

        return value.isEmpty()
                || value.contains("확인 어려움")
                || value.contains("판단 어려움")
                || value.contains("없음")
                || value.contains("미확인");
    }

    private String getStringValue(Map<String, Object> map, String key) {

        if (map == null || key == null) {
            return "";
        }

        Object value = map.get(key);

        if (value == null) {
            return "";
        }

        return CmmUtil.nvl(String.valueOf(value));
    }

    private List<String> splitKeyword(String text) {

        text = CmmUtil.nvl(text)
                .replace("/", " ")
                .replace(",", " ")
                .replace(".", " ")
                .replace("·", " ")
                .replace("-", " ")
                .replace("_", " ")
                .replace("(", " ")
                .replace(")", " ");

        String[] arr = text.split("\\s+");

        List<String> rList = new ArrayList<>();

        for (String word : arr) {

            String value = CmmUtil.nvl(word).trim();

            if (!value.isEmpty()) {
                rList.add(value);
            }
        }

        return rList;
    }

    private String normalizeWithSynonym(String text) {

        text = normalize(text);

        return text
                .replace("노란색", "황색")
                .replace("노란", "황색")
                .replace("노랗", "황색")
                .replace("누런", "황색")
                .replace("황화", "황색변색")

                .replace("하얀색", "흰색")
                .replace("하얀", "흰색")
                .replace("백색", "흰색")

                .replace("검정색", "검은색")
                .replace("흑색", "검은색")

                .replace("갈변", "갈색변색")
                .replace("갈색반점", "갈색병반")
                .replace("반점", "병반")
                .replace("점무늬", "병반")
                .replace("둥근무늬", "원형병반")

                .replace("구멍", "식흔")
                .replace("갉아먹은흔적", "식흔")
                .replace("갉아먹음", "식흔")
                .replace("먹은흔적", "식흔")

                .replace("말림", "위축")
                .replace("오그라듦", "위축")
                .replace("쭈글", "위축")

                .replace("곰팡이", "균사")
                .replace("흰가루", "흰색균사")
                .replace("가루", "균사")

                .replace("시듦", "위조")
                .replace("마름", "고사");
    }

    private String normalize(String text) {

        return CmmUtil.nvl(text)
                .toLowerCase()
                .replace(" ", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")
                .replace(",", "")
                .replace(".", "")
                .replace("·", "")
                .replace("-", "")
                .replace("_", "")
                .replace("(", "")
                .replace(")", "")
                .replace("[", "")
                .replace("]", "")
                .replace("{", "")
                .replace("}", "")
                .trim();
    }
}