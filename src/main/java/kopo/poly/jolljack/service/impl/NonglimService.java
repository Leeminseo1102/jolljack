package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.NonglimApiItemDTO;
import kopo.poly.jolljack.dto.NonglimApiReDTO;
import kopo.poly.jolljack.dto.NonglimDTO;
import kopo.poly.jolljack.service.INonglimService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonglimService implements INonglimService {

    @Value("${public.data.api-key}")
    private String apiKey;

    private static final String API_URL =
            "https://api.odcloud.kr/api/15050900/v1/uddi:9aab997b-d0a1-432f-8095-e1037469942d_201909251743";

    private static final int PER_PAGE = 500;

    private static final List<String> TARGET_CROPS =
            List.of("사과", "감자", "포도", "고추", "배추", "벼");

    @Override
    public List<NonglimDTO> getNonglimList() throws Exception {

        log.info("{}.getNonglimList Start!", this.getClass().getName());

        List<NonglimDTO> rList = new ArrayList<>();

        try {

            String url = API_URL
                    + "?page=1"
                    + "&perPage=" + PER_PAGE
                    + "&returnType=JSON"
                    + "&serviceKey=" + apiKey;

            log.info("request url : {}", url);

            RestClient restClient = RestClient.create();

            NonglimApiReDTO response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(NonglimApiReDTO.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.info("{}.getNonglimList data empty", this.getClass().getName());
                return rList;
            }

            for (NonglimApiItemDTO item : response.getData()) {

                String title = CmmUtil.nvl(item.getTitle());
                String cropName = extractCropName(title);

                if (!TARGET_CROPS.contains(cropName)) {
                    continue;
                }

                String cultivationText = refineCultivationText(item);

                if (cultivationText.isBlank()) {
                    continue;
                }

                NonglimDTO rDTO = NonglimDTO.builder()
                        .cropName(cropName)
                        .cultivationText(cultivationText)
                        .build();

                rList.add(rDTO);
            }

            log.info("농림 API 전체 데이터 수 : {}", response.getTotalCount());
            log.info("농림 API 정제 작물 개수 : {}", rList.size());
            log.info("{}.getNonglimList End!", this.getClass().getName());

            return rList;

        } catch (Exception e) {
            log.info("{}.getNonglimList Error : {}", this.getClass().getName(), e.getMessage(), e);
            return rList;
        }
    }

    @Override
    public String getNonglimPrompt() throws Exception {

        log.info("{}.getNonglimPrompt Start!", this.getClass().getName());

        List<NonglimDTO> rList = getNonglimList();

        StringBuilder sb = new StringBuilder();

        sb.append("[농림수산식품교육문화원정보원 작물재배 정보 API 참고 데이터]\n");
        sb.append("아래 내용은 공공데이터 API에서 조회한 작물별 재배상 유의점입니다.\n");
        sb.append("추천 작물의 재배방법을 작성할 때 아래 내용을 참고하되, 지역 날씨 조건에 맞게 자연스럽게 설명하세요.\n\n");

        for (NonglimDTO dto : rList) {
            sb.append(dto.getCropName()).append(":\n");
            sb.append(dto.getCultivationText()).append("\n");
        }

        log.info("{}.getNonglimPrompt End!", this.getClass().getName());

        return sb.toString();
    }

    private String extractCropName(String title) {

        title = CmmUtil.nvl(title).trim();

        if (title.contains("(")) {
            return title.substring(0, title.indexOf("(")).trim();
        }

        return title;
    }

    private String refineCultivationText(NonglimApiItemDTO item) {

        StringBuilder sb = new StringBuilder();

        appendContent(sb, item.getSubTitle1(), item.getSubContent1());
        appendContent(sb, item.getSubTitle2(), item.getSubContent2());
        appendContent(sb, item.getSubTitle3(), item.getSubContent3());
        appendContent(sb, item.getSubTitle4(), item.getSubContent4());
        appendContent(sb, item.getSubTitle5(), item.getSubContent5());

        return sb.toString();
    }

    private void appendContent(StringBuilder sb, String subTitle, String subContent) {

        subTitle = cleanText(subTitle);
        subContent = cleanText(subContent);

        if (subContent.isBlank()) {
            return;
        }

        if (!isUsefulTitle(subTitle)) {
            return;
        }

        if (subContent.length() > 500) {
            subContent = subContent.substring(0, 500);
        }

        sb.append("- ");

        if (!subTitle.isBlank()) {
            sb.append(subTitle).append(": ");
        }

        sb.append(subContent).append("\n");
    }

    private boolean isUsefulTitle(String subTitle) {

        subTitle = CmmUtil.nvl(subTitle);

        return subTitle.contains("재배")
                || subTitle.contains("유의사항")
                || subTitle.contains("유의점");
    }

    private String cleanText(String text) {

        text = CmmUtil.nvl(text);

        return text.replaceAll("<br\\s*/?>", " ")
                .replaceAll("<.*?>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}