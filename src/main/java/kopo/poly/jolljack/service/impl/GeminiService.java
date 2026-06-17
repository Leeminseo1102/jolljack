package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.jolljack.service.IGeminiService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements IGeminiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=";


    //분석

    @Override
    public GeminiCropReDTO getCropAnalysis(WeatherSumDTO pDTO, String nonglimPrompt) throws Exception {

        log.info("{}.getCropAnalysis Start!", this.getClass().getName());

        String prompt = buildPrompt(pDTO, nonglimPrompt);

        String response = callGemini(prompt);

        GeminiCropReDTO rDTO = parseResponse(response);

        log.info("{}.getCropAnalysis End!", this.getClass().getName());

        return rDTO;
    }


    private String buildPrompt(WeatherSumDTO dto, String nonglimPrompt) {

        return "다음 날씨 조건을 기반으로"
                + "벼, 감자, 고추, 배추, 포도, 사과 대한 현재 날씨 기준 적합도를 판단하고,\n"
                + "그 중 가장 적합한 작물 1개를 선택할 것.\n"

                + "[중요 규칙]\n"
                + "현재 제공된 날씨 데이터만을 기준으로 판단하되, 최근 1년 데이터는 기본 적합성 판단 기준, 최근 6개월 데이터는 추천 작물 선정의 핵심 기준, 최근 3개월 데이터는 현재 리스크와 단기 보정 기준으로 사용한다.\n"
                + "작물 선택은 최근 3개월 값만으로 결정하지 말고, 반드시 최근 1년과 최근 6개월 값을 함께 반영하여 판단한다.\n"
                + "비슷한 조건이라도 지역별 기온, 습도, 강수량 차이를 반영하여 지역 특성에 따라 다양한 결과가 나오도록 한다.\n"
                + "일반적인 재배 이론, 계절 전체 설명, 불필요한 배경 설명은 제외하고 현재 시점에서 실제로 해야 할 행동 중심으로 작성한다.\n"
                + "수치는 주어진 값만 사용하며 임의 해석하지 않는다.\n"
                + "출력은 한국어로만 자연스럽고 부드러운 설명체(~입니다, ~습니다)로 작성하고 단정형 표현(~이다, ~한다), "
                + "영어, 강조 기호(**), 특수기호(-, •), 라벨형 표현은 사용하지 않는다.\n"
                + "리스크(5개의 리스크 총합이 100이 되도록)의 지문의 반환 값에 `리스크(5개의 리스크 총합이 100이 되도록)`이 문장을 넣지 말도록 \n"
                + "재배 방법은 단계별로 구분하여 작성한다.\n\n"

                + "[출력 형식]\n"
                + "작물명:\n"

                + "추천이유:\n"
                + "- 현재 기온(숫자), 습도(숫자), 강수량(숫자), 풍속(숫자)을 기반으로 왜 적합한지 자연스럽게 설명할 것\n"
                + "- '조건 → 의미 → 결론' 구조로 작성할 것\n"
                + "- 단기 날씨 기준임을 문장 안에서 자연스럽게 포함할 것\n"
                + "- 일조량은 단위와 기간이 명확하지 않으면 해석하지 말 것\n"
                + "- 과장된 표현 금지, 객관적인 판단 형태로 작성\n"

                + "재배방법:\n"
                + "- 반드시 현재 날씨 기준으로 지금 해야 할 작업만 작성\n"
                + "- '~해야 한다' 형태로 작성\n"
                + "- 최소 4개 이상 구체적인 행동 작성\n"
                + "- 동해 방지, 전정, 토양 관리 등 실제 작업 위주\n\n"

                + "리스크(5개의 리스크 총합이 100이 되도록):\n"
                + "1. 내용 - 점수\n"
                + "2. 내용 - 점수\n"
                + "3. 내용 - 점수\n"
                + "4. 내용 - 점수\n"
                + "5. 내용 - 점수\n"

                + "작물 추천 점수:(0~100 사이 숫자만)\n\n"

                + "[작물재배 정보 API 참고 데이터]\n"
                + CmmUtil.nvl(nonglimPrompt) + "\n\n"

                + "[날씨 정보]\n"
                + "지역: " + dto.getFullRegionName() + "\n"

                + "[최근 1년]\n"
                + "평균기온: " + dto.getYearAvgTemp() + "\n"
                + "최고기온: " + dto.getYearMaxTemp() + "\n"
                + "최저기온: " + dto.getYearMinTemp() + "\n"
                + "습도: " + dto.getYearHumidity() + "\n"
                + "강수량: " + dto.getYearRainfall() + "\n"
                + "풍속: " + dto.getYearWindSpeed() + "\n"
                + "일조량: " + dto.getYearSunshine() + "\n"

                + "[최근 6개월]\n"
                + "평균기온: " + dto.getSixMonthAvgTemp() + "\n"
                + "최고기온: " + dto.getSixMonthMaxTemp() + "\n"
                + "최저기온: " + dto.getSixMonthMinTemp() + "\n"
                + "습도: " + dto.getSixMonthHumidity() + "\n"
                + "강수량: " + dto.getSixMonthRainfall() + "\n"
                + "풍속: " + dto.getSixMonthWindSpeed() + "\n"
                + "일조량: " + dto.getSixMonthSunshine() + "\n"

                + "[최근 3개월]\n"
                + "평균기온: " + dto.getThreeMonthAvgTemp() + "\n"
                + "최고기온: " + dto.getThreeMonthMaxTemp() + "\n"
                + "최저기온: " + dto.getThreeMonthMinTemp() + "\n"
                + "습도: " + dto.getThreeMonthHumidity() + "\n"
                + "강수량: " + dto.getThreeMonthRainfall() + "\n"
                + "풍속: " + dto.getThreeMonthWindSpeed() + "\n"
                + "일조량: " + dto.getThreeMonthSunshine();
    }


    private String callGemini(String prompt) throws Exception {


        RestClient restClient = RestClient.create();

        // JSON 문자열에 들어갈 프롬프트 escape 처리
        String escapedPrompt = prompt
                .replace("\\", "\\\\")   // 백슬래시 이스케이프
                .replace("\"", "\\\"")   // 따옴표 이스케이프
                .replace("\n", "\\n");   // 줄바꿈 이스케이프

        // Gemini 요청 바디 (JSON 직접 구성)
        String body = "{"
                + "\"contents\":[{"
                + "\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]"
                + "}]"
                + "}";


        String url = GEMINI_URL + apiKey;

        try {

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (!CmmUtil.nvl(response).isEmpty()) {
                return extractText(response);
            }

            throw new Exception("Gemini 응답 데이터가 비어 있습니다.");

        } catch (Exception e) {

            log.info("Gemini API 호출 실패 : {}", e.getMessage(), e);

            throw new Exception("Gemini API 호출 중 오류가 발생했습니다.", e);
        }
    }


    private String extractText(String json) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        GeminiReDTO rDTO = mapper.readValue(json, GeminiReDTO.class);

        if (rDTO == null
                || rDTO.candidates() == null
                || rDTO.candidates().isEmpty()) {

            throw new Exception("Gemini 응답 데이터가 없습니다.");
        }

        GeminiReDTO.CandidateDTO candidateDTO = rDTO.candidates().get(0);

        if (candidateDTO == null
                || candidateDTO.content() == null
                || candidateDTO.content().parts() == null
                || candidateDTO.content().parts().isEmpty()) {

            throw new Exception("Gemini 응답 내용이 없습니다.");
        }

        GeminiReDTO.PartDTO partDTO = candidateDTO.content().parts().get(0);

        if (partDTO == null || CmmUtil.nvl(partDTO.text()).isEmpty()) {
            throw new Exception("Gemini 응답 text가 비어 있습니다.");
        }

        return partDTO.text();
    }


    private GeminiCropReDTO parseResponse(String response) {

        String cropName = extractSection(response, "작물명:", "추천이유:");
        String reason = extractSection(response, "추천이유:", "재배방법:");
        String method = extractSection(response, "재배방법:", "리스크:");
        String risk = extractSection(response, "리스크:", "점수:");
        String scoreStr = extractSection(response, "점수:", null);

        int score = 0;

        try {
            String onlyNumber = CmmUtil.nvl(scoreStr).replaceAll("[^0-9]", "").trim();

            if (!onlyNumber.isEmpty()) {
                score = Integer.parseInt(onlyNumber);
            }

        } catch (Exception e) {
            log.info("score parse fail : {}", e.getMessage(), e);
        }

        return GeminiCropReDTO.builder()
                .recommendedCropName(CmmUtil.nvl(cropName).trim())
                .recommendedReason(CmmUtil.nvl(reason).trim())
                .cultivationMethod(CmmUtil.nvl(method).trim())
                .riskSummary(CmmUtil.nvl(risk).trim())
                .score(score)
                .build();
    }


    private String extractSection(String text, String startLabel, String endLabel) {

        if (text == null || text.isEmpty()) {
            return "";
        }

        int start = text.indexOf(startLabel);

        if (start < 0) {
            return "";
        }

        start += startLabel.length();

        int end;

        if (endLabel == null) {
            end = text.length();

        } else {
            end = text.indexOf(endLabel, start);

            if (end < 0) {
                end = text.length();
            }
        }

        return text.substring(start, end).trim();
    }

    // 진단
    //1차
    @Override
    public GeminiReDiagDTO getDiagFeature(ImgGeminiDTO pDTO) throws Exception {

        log.info("{}.getDiagFeature Start!", this.getClass().getName());

        if (pDTO == null) {
            throw new Exception("Gemini 이미지 요청 정보가 없습니다.");
        }

        String mimeType = CmmUtil.nvl(pDTO.mimeType());
        String fileUri = CmmUtil.nvl(pDTO.fileUri());

        if (mimeType.isEmpty()) {
            throw new Exception("이미지 MIME 타입이 없습니다.");
        }

        if (fileUri.isEmpty()) {
            throw new Exception("이미지 URL 정보가 없습니다.");
        }

        String prompt = getDiagFeaturePrompt();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of(
                                                "fileData", Map.of(
                                                        "mimeType", mimeType,
                                                        "fileUri", fileUri
                                                )
                                        ),
                                        Map.of(
                                                "text", prompt
                                        )
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "topP", 0.8,
                        "topK", 40,
                        "maxOutputTokens", 2048,
                        "responseMimeType", "application/json"
                )
        );

        String url = GEMINI_URL + apiKey;

        try {

            RestClient restClient = RestClient.create();

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (CmmUtil.nvl(response).isEmpty()) {
                throw new Exception("Gemini 응답 데이터가 비어 있습니다.");
            }

            String featureJson = extractText(response);

            ObjectMapper mapper = new ObjectMapper();

            GeminiReDiagDTO rDTO = mapper.readValue(featureJson, GeminiReDiagDTO.class);

            log.info("Gemini diag visualSummary : {}", CmmUtil.nvl(rDTO.visualSummary()));
            log.info("{}.getDiagFeature End!", this.getClass().getName());

            return rDTO;

        } catch (Exception e) {

            log.info("Gemini 병충해 이미지 특징 추출 실패 : {}", e.getMessage(), e);

            throw new Exception("Gemini 병충해 이미지 특징 추출 중 오류가 발생했습니다.", e);
        }
    }

    private String getDiagFeaturePrompt() {

        return """
            너는 농작물 병충해 이미지에서 시각적 특징을 추출하는 분석 도우미다.

            이 작업의 목적:
            - 병명이나 해충명을 맞히는 것이 아니다.
            - 작물명을 추정하는 것도 아니다.
            - 이미지에서 보이는 병충해 관련 시각 특징을 추출하여,
              이후 서버의 공공데이터 병충해 API 증상 데이터와 비교하기 위한 키워드로 사용한다.

            반드시 지켜야 할 규칙:
            1. 작물명을 추정하지 마라.
            2. 병명, 해충명, 진단명, 확률, 정확도 작성하지 마라.
            3. 이미지에서 실제로 보이는 시각 특징만 작성해라.
            4. 보이지 않는 내용은 추측하지 마라.
            5. 모든 필드는 반드시 포함해라.
            6. 배열 필드는 값이 없거나 판단이 어려우면 ["확인 어려움"]을 넣어라.
            7. 반드시 JSON 객체 하나만 응답해라.
            8. 마크다운, 코드블록, ```json, 추가 설명 문장을 절대 넣지 마라.

            JSON 형식은 반드시 아래 구조를 그대로 따른다.

            {
              "affectedParts": ["잎"],
              "symptomKeywords": ["갈색 반점", "노란 변색", "잎 말림"],
              "colorKeywords": ["갈색", "노란색"],
              "shapeKeywords": ["원형", "불규칙형", "가장자리 번짐"],
              "damageTypes": ["반점", "변색", "말림"],
              "pestSigns": ["해충 개체 확인 어려움", "갉아먹은 흔적 확인 어려움"],
              "feedingPatterns": ["확인 어려움"],
              "diseaseSigns": ["병반 의심", "잎 변색"],
              "spreadPatterns": ["점 형태로 산발적 발생", "일부 병반 주변 노란 변색"],
              "visualSummary": "잎 표면에 갈색 반점과 노란 변색이 보이며, 일부 잎은 말린 형태를 보입니다."
            }

            필드 작성 기준:
            - affectedParts: 피해가 보이는 부위. 예: 잎, 줄기, 열매, 꽃, 뿌리, 전체
            - symptomKeywords: 이미지에서 보이는 증상 키워드. 예: 갈색 반점, 노란 변색, 잎 말림, 시듦, 구멍, 곰팡이
            - colorKeywords: 증상 부위의 색상. 예: 갈색, 노란색, 검은색, 흰색, 회색, 연녹색
            - shapeKeywords: 병반, 구멍, 피해 흔적의 형태. 예: 원형, 타원형, 불규칙형, 점무늬, 줄무늬, 가장자리 번짐
            - damageTypes: 피해 유형. 예: 반점, 변색, 말림, 구멍, 갉아먹힘, 부패, 시듦, 곰팡이, 마름
            - pestSigns: 해충 관련 시각 흔적. 예: 벌레 보임, 알 보임, 배설물 의심, 갉아먹은 흔적, 잎맥만 남음
            - feedingPatterns: 해충이 남긴 식흔이나 섭식 패턴. 예: 원형으로 갉아먹음, 가장자리부터 갉아먹음, 작은 구멍 다수, 불규칙한 식흔
            - diseaseSigns: 병해 관련 시각 흔적. 예: 병반 의심, 곰팡이 의심, 균사 의심, 수침상 병반, 검은 반점, 흰 가루
            - spreadPatterns: 병반 또는 변색이 퍼진 방식. 예: 중심부에서 바깥으로 번짐, 가장자리부터 변색, 점 형태로 산발적 발생
            - visualSummary: 이미지에서 실제로 보이는 특징만 1~2문장으로 요약

            해충 피해가 뚜렷하면 feedingPatterns를 구체적으로 작성해라.
            병해 피해가 뚜렷하면 diseaseSigns와 spreadPatterns를 구체적으로 작성해라.
            둘 중 구분이 어렵다면 각각 ["확인 어려움"]을 넣어라.
            """;
    }

    //2차
    @Override
    public DiDiDTO getDiagFinalResultProc(GeminiReDiagDTO featureDTO, List<PapCandidateDTO> top3List) throws Exception {

        log.info("{}.getDiagFinalResultProc Start!", this.getClass().getName());

        if (featureDTO == null) {
            throw new IllegalArgumentException("Gemini 1차 특징 추출 결과가 없습니다.");
        }

        if (top3List == null || top3List.isEmpty()) {
            throw new IllegalArgumentException("병충해 후보 Top 3 결과가 없습니다.");
        }

        String prompt = getDiagFinalPrompt(featureDTO, top3List);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "topP", 0.8,
                        "topK", 40,
                        "maxOutputTokens", 2048,
                        "responseMimeType", "application/json"
                )
        );

        String url = GEMINI_URL + apiKey;

        try {

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (CmmUtil.nvl(response).isEmpty()) {
                throw new Exception("Gemini 2차 응답 데이터가 비어 있습니다.");
            }

            String resultJson = extractText(response);

            DiDiDTO rDTO = objectMapper.readValue(resultJson, DiDiDTO.class);

            log.info("Gemini diag final disease : {}", CmmUtil.nvl(rDTO.predictedDiseaseName()));
            log.info("{}.getDiagFinalResultProc End!", this.getClass().getName());

            return rDTO;

        } catch (Exception e) {

            log.info("Gemini 병충해 최종 진단 실패 : {}", e.getMessage(), e);

            throw new Exception("Gemini 병충해 최종 진단 중 오류가 발생했습니다.", e);
        }
    }

    private String getDiagFinalPrompt(GeminiReDiagDTO featureDTO, List<PapCandidateDTO> top3List) {

        StringBuilder candidateText = new StringBuilder();

        for (int i = 0; i < top3List.size(); i++) {

            PapCandidateDTO candidate = top3List.get(i);

            candidateText.append(i + 1).append("순위 후보\n")
                    .append("- 병충해명: ").append(CmmUtil.nvl(candidate.diseaseName())).append("\n")
                    .append("- 매칭 점수: ").append(candidate.score()).append("점\n")
                    .append("- 매칭 키워드: ").append(candidate.matchedKeywords()).append("\n")
                    .append("- API 특징: ").append(CmmUtil.nvl(candidate.feature())).append("\n")
                    .append("- 발생시기내용: ").append(CmmUtil.nvl(candidate.occurrenceContent())).append("\n")
                    .append("- 방제내용: ").append(CmmUtil.nvl(candidate.controlContent())).append("\n\n");
        }

        return """
            너는 농작물 병충해 이미지 진단 전문가다.

            아래 정보는 Gemini가 이미지에서 추출한 시각적 특징과,
            공공데이터 병충해 API 데이터와 서버에서 비교하여 선별한 후보 3개다.

            반드시 아래 후보 3개 안에서만 최종 병충해명을 선택해야 한다.
            후보에 없는 병충해명을 새로 만들지 마라.
            작물명을 새로 추정하지 마라.
            보이지 않는 증상을 단정하지 마라.

            응답은 반드시 JSON 객체 하나만 반환해라.
            마크다운 코드블록을 사용하지 마라.
            ```json 같은 문자를 절대 쓰지 마라.
            JSON 밖에 설명 문장을 쓰지 마라.

            JSON 필드명은 반드시 아래와 동일해야 한다.

            {
              "predictedDiseaseName": "",
              "diseaseDescription": "",
              "solutionText": "",
              "symptomSummary": "",
              "preventionText": ""
            }
            
            [공공데이터 API 비교 결과 Top 3]
            """
                + candidateText
                + """

            작성 규칙:
            1. predictedDiseaseName에는 후보 3개 병충해명을 작성해라, 작성할때 매칭이 높은 순서부터 적어라.
            예 : 1순위 고추 탄저병, 2순위 고추 역병, 3순위 흰가루병
            2. diseaseDescription에는 병충해가 어떤 병충해인지 자세하게  설명해라, 작성할때 매칭이 높은 순서부터 적어라.
            예 : 1순위 설명, 2순위 설명, 3순위 설명
            3. solutionText에는 API 방제내용을 기반하여 실제 방제 방법과 조치 방법을 자세하게 길게 작성해라, 작성할때 매칭이 높은 순서부터 적어라.
            예 : 1순위  방제, 2순위  방제, 3순위  방제
            4. symptomSummary에는 이미지 특징 요약과 후보별 매칭 점수를 함께 작성해라.
               예: "이미지에서 갈색 병반과 잎 마름 증상이 확인됩니다. 후보별 유사도 점수는 1순위 고추 탄저병 87점, 2순위 고추 역병 72점, 3순위 흰가루병 61점입니다."
            5. preventionText에는 재발 방지와 예방 관리 방법을 자세하게 길게 작성해라, 작성할때 매칭이 높은 순서부터 적어라.
            6. 모든 값은 한국어 문장으로 작성해라.
            """;
    }


}