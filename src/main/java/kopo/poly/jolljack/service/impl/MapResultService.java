package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropAnalysisDTO;
import kopo.poly.jolljack.mapper.ICropAnalysisMapper;
import kopo.poly.jolljack.service.IMapResultService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapResultService implements IMapResultService {

    private final ICropAnalysisMapper cropAnalysisMapper;

    private static final String SESSION_USER_ID = "userId";

    @Override
    public Map<String, Object> getResultDataProc(CropAnalysisDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.getResultDataProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);

        if (userId == null) {
            rMap.put("result", "LOGIN_REQUIRED");
            rMap.put("msg", getMsg("LOGIN_REQUIRED"));

            log.info("{}.getResultDataProc End!", this.getClass().getName());

            return rMap;
        }

        if (pDTO == null || pDTO.getAnalysisId() == null) {
            rMap.put("result", "ANALYSIS_ID_EMPTY");
            rMap.put("msg", getMsg("ANALYSIS_ID_EMPTY"));

            log.info("{}.getResultDataProc End!", this.getClass().getName());

            return rMap;
        }

        CropAnalysisDTO analysisDTO = CropAnalysisDTO.builder()
                .analysisId(pDTO.getAnalysisId())
                .userId(userId)
                .build();

        CropAnalysisDTO rDTO = cropAnalysisMapper.getCropAnalysisResult(analysisDTO);

        if (rDTO == null || rDTO.getAnalysisId() == null) {
            rMap.put("result", "ANALYSIS_NOT_FOUND");
            rMap.put("msg", getMsg("ANALYSIS_NOT_FOUND"));

            log.info("{}.getResultDataProc End!", this.getClass().getName());

            return rMap;
        }

        rMap.put("result", "RESULT_OK");
        rMap.put("msg", getMsg("RESULT_OK"));
        rMap.put("analysisId", rDTO.getAnalysisId());
        rMap.put("userId", rDTO.getUserId());
        rMap.put("regionId", rDTO.getRegionId());
        rMap.put("recommendedCropId", rDTO.getRecommendedCropId());
        rMap.put("cropName", CmmUtil.nvl(rDTO.getCropName()));
        rMap.put("fullRegionName", CmmUtil.nvl(rDTO.getFullRegionName()));
        rMap.put("recommendedReason", CmmUtil.nvl(rDTO.getRecommendedReason()));
        rMap.put("cultivationMethod", CmmUtil.nvl(rDTO.getCultivationMethod()));
        rMap.put("riskSummary", CmmUtil.nvl(rDTO.getRiskSummary()));
        rMap.put("weatherSummary", CmmUtil.nvl(rDTO.getWeatherSummary()));
        rMap.put("score", rDTO.getScore());
        rMap.put("createdAt", CmmUtil.nvl(rDTO.getCreatedAt()));

        log.info("{}.getResultDataProc End!", this.getClass().getName());

        return rMap;
    }

    private String getMsg(String code) {
        return switch (code) {
            case "LOGIN_REQUIRED" -> "로그인 후 이용 가능합니다.";
            case "ANALYSIS_ID_EMPTY" -> "분석 번호가 없습니다.";
            case "ANALYSIS_NOT_FOUND" -> "분석 결과를 찾을 수 없습니다.";
            case "RESULT_OK" -> "분석 결과 조회 성공";
            default -> "오류가 발생했습니다.";
        };
    }
}