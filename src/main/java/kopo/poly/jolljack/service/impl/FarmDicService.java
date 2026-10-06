package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.FarmDicDetailDTO;
import kopo.poly.jolljack.dto.FarmDicSearchDTO;
import kopo.poly.jolljack.service.IFarmDicService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmDicService implements IFarmDicService {

    @Value("${nongsaro.api-key}")
    private String apiKey;

    private static final String SEARCH_API_URL =
            "http://api.nongsaro.go.kr/service/farmDic/searchFrontMatch";

    private static final String DETAIL_API_URL =
            "http://api.nongsaro.go.kr/service/farmDic/detailWord";

    private static final String WORD_TYPE =
            "A";

    private static final int NUM_OF_ROWS =
            100;

    private static final String NORTH_KOREA_LANG_CODE =
            "108006";


    @Override
    public List<FarmDicSearchDTO> searchFrontMatch(String word, int pageNo) throws Exception {

        log.info("{}.searchFrontMatch Start!", this.getClass().getName());

        String searchWord =
                CmmUtil.nvl(word).trim();

        if (searchWord.isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        if (pageNo < 1) {
            pageNo = 1;
        }

        URI uri = UriComponentsBuilder
                .fromUriString(SEARCH_API_URL)
                .queryParam("apiKey", apiKey)
                .queryParam("word", searchWord)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("wordType", WORD_TYPE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        RestClient restClient =
                RestClient.create();

        String response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (CmmUtil.nvl(response).isEmpty()) {
            throw new IllegalStateException("농업용어사전 검색 결과를 불러오지 못했습니다.");
        }

        Document document =
                parseXml(response);

        validateApiResult(document);

        NodeList itemNodes =
                document.getElementsByTagName("item");

        List<FarmDicSearchDTO> rList =
                new ArrayList<>();

        for (int i = 0; i < itemNodes.getLength(); i++) {

            Node node =
                    itemNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element item =
                    (Element) node;

            String langCode =
                    getTagValue(
                            item,
                            "langCode"
                    );

            if (NORTH_KOREA_LANG_CODE.equals(langCode)) {
                continue;
            }

            FarmDicSearchDTO rDTO =
                    new FarmDicSearchDTO();

            rDTO.setWordNo(
                    getTagValue(
                            item,
                            "wordNo"
                    )
            );

            rDTO.setLangCode(
                    langCode
            );

            rDTO.setLangNm(
                    getTagValue(
                            item,
                            "langNm"
                    )
            );

            rDTO.setWordNm(
                    getTagValue(
                            item,
                            "wordNm"
                    )
            );

            rDTO.setWordType(
                    getTagValue(
                            item,
                            "wordType"
                    )
            );

            rList.add(
                    rDTO
            );
        }

        log.info("농업용어사전 검색 word : {}, apiPage : {}, 조회건수 : {}",
                searchWord,
                pageNo,
                rList.size());

        log.info("{}.searchFrontMatch End!", this.getClass().getName());

        return rList;
    }


    @Override
    public FarmDicDetailDTO detailWord(String wordNo) throws Exception {

        log.info("{}.detailWord Start!", this.getClass().getName());

        String searchWordNo =
                CmmUtil.nvl(wordNo).trim();

        if (searchWordNo.isEmpty()) {
            throw new IllegalArgumentException("농업 용어 번호가 없습니다.");
        }

        URI uri = UriComponentsBuilder
                .fromUriString(DETAIL_API_URL)
                .queryParam("apiKey", apiKey)
                .queryParam("wordNo", searchWordNo)
                .queryParam("wordType", WORD_TYPE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        RestClient restClient =
                RestClient.create();

        String response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (CmmUtil.nvl(response).isEmpty()) {
            throw new IllegalStateException("농업용어사전 상세 정보를 불러오지 못했습니다.");
        }

        Document document =
                parseXml(response);

        validateApiResult(document);

        NodeList itemNodes =
                document.getElementsByTagName("item");

        if (itemNodes.getLength() == 0) {

            log.info("농업용어사전 상세 조회 결과 없음 wordNo : {}", searchWordNo);

            log.info("{}.detailWord End!", this.getClass().getName());

            return null;
        }

        Element item =
                (Element) itemNodes.item(0);

        FarmDicDetailDTO rDTO =
                new FarmDicDetailDTO();

        rDTO.setFarmngWordNo(
                getTagValue(
                        item,
                        "farmngWordNo"
                )
        );

        rDTO.setWordDc(
                getTagValue(
                        item,
                        "wordDc"
                )
        );

        log.info("농업용어사전 상세 조회 wordNo : {}", searchWordNo);

        log.info("{}.detailWord End!", this.getClass().getName());

        return rDTO;
    }


    private Document parseXml(String xml) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setXIncludeAware(false);

        factory.setExpandEntityReferences(false);

        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)
                );

        Document document =
                factory.newDocumentBuilder()
                        .parse(inputStream);

        document.getDocumentElement()
                .normalize();

        return document;
    }


    private void validateApiResult(Document document) {

        NodeList resultCodeNodes =
                document.getElementsByTagName("resultCode");

        if (resultCodeNodes.getLength() == 0) {
            throw new IllegalStateException("농사로 API 응답 코드를 확인할 수 없습니다.");
        }

        String resultCode =
                CmmUtil.nvl(
                        resultCodeNodes.item(0).getTextContent()
                ).trim();

        if ("00".equals(resultCode)) {
            return;
        }

        NodeList resultMsgNodes =
                document.getElementsByTagName("resultMsg");

        String resultMsg =
                resultMsgNodes.getLength() > 0
                        ? CmmUtil.nvl(resultMsgNodes.item(0).getTextContent()).trim()
                        : "";

        throw new IllegalStateException(
                "농사로 API 호출 실패 : "
                        + resultCode
                        + " "
                        + resultMsg
        );
    }


    private String getTagValue(Element element, String tagName) {

        NodeList nodeList =
                element.getElementsByTagName(tagName);

        if (nodeList.getLength() == 0) {
            return "";
        }

        return CmmUtil.nvl(
                nodeList.item(0).getTextContent()
        ).trim();
    }
}