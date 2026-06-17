package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.service.INonglimService;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropAnalysisDTO;
import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.GeminiCropReDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.WeatherSumDTO;
import kopo.poly.jolljack.mapper.ICropAnalysisMapper;
import kopo.poly.jolljack.mapper.ISignupMapper;
import kopo.poly.jolljack.service.ICropLoadingService;
import kopo.poly.jolljack.service.IGeminiService;
import kopo.poly.jolljack.service.IMAHubService;
import kopo.poly.jolljack.service.IMAService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CropLoadingService implements ICropLoadingService {

    private final ISignupMapper signupMapper;
    private final IMAService maService;
    private final IMAHubService maHubService;
    private final IGeminiService geminiService;
    private final ICropAnalysisMapper cropAnalysisMapper;
    private final INonglimService nonglimService;

    private static final String SESSION_ANALYSIS_REGION_ID = "analysisRegionId";
    private static final String SESSION_USER_ID = "userId";

    @Override
    public Map<String, Object> cropLoadingProc(HttpSession session) throws Exception {

        log.info("{}.cropLoadingProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Long regionId = (Long) session.getAttribute(SESSION_ANALYSIS_REGION_ID);

        if (regionId == null) {
            rMap.put("result", "SESSION_INVALID");
            rMap.put("msg", "선택한 지역 정보가 없습니다.");
            return rMap;
        }

        RegionDTO regionDTO = signupMapper.getRegionByRegionId(
                RegionDTO.builder()
                        .regionId(regionId)
                        .build()
        );

        if (regionDTO == null || regionDTO.getRegionId() == null) {
            rMap.put("result", "REGION_NOT_FOUND");
            rMap.put("msg", "지역 정보를 찾을 수 없습니다.");
            return rMap;
        }

        WeatherSumDTO weatherDTO = maService.getMonthWeather(regionDTO);

        String weatherResult = weatherDTO.getResult();

        if (!"MA_OK".equals(weatherResult)) {
            log.info("MAService fallback to MAHubService. result : {}", weatherResult);
            weatherDTO = maHubService.getDailyWeather(regionDTO);
        }

        if (!"MA_OK".equals(weatherDTO.getResult()) && !"HUB_OK".equals(weatherDTO.getResult())) {
            rMap.put("result", weatherDTO.getResult());
            rMap.put("msg", weatherDTO.getMsg());
            return rMap;
        }

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);

        if (userId == null) {
            rMap.put("result", "LOGIN_REQUIRED");
            rMap.put("msg", "로그인 후 이용 가능합니다.");
            return rMap;
        }
        //기본값 농림 API 실패하면
        //그냥 빈 문자열로 Gemini 호출 목적으로 한거임 까먹지 말자
        String nonglimPrompt = "";

        try {
            nonglimPrompt = nonglimService.getNonglimPrompt();

            log.info("농림 API 재배 정보 조회 성공");
            log.info("nonglimPrompt length : {}", nonglimPrompt.length());

        } catch (Exception e) {
            log.info("농림 API 재배 정보 조회 실패, 기본 Gemini 프롬프트로 진행 : {}", e.getMessage(), e);
        }

        GeminiCropReDTO geminiDTO = geminiService.getCropAnalysis(weatherDTO, nonglimPrompt);

        String recommendedCropName = CmmUtil.nvl(geminiDTO.getRecommendedCropName()).trim();

        if (recommendedCropName.isEmpty()) {
            rMap.put("result", "GEMINI_CROP_EMPTY");
            rMap.put("msg", "추천 작물 정보를 받지 못했습니다.");
            return rMap;
        }

        CropDTO cropDTO = signupMapper.getCropByCropName(
                CropDTO.builder()
                        .cropName(recommendedCropName)
                        .build()
        );

        if (cropDTO == null || cropDTO.getCropId() == null) {
            rMap.put("result", "CROP_NOT_FOUND");
            rMap.put("msg", "추천 작물 정보를 찾을 수 없습니다.");
            return rMap;
        }

        CropAnalysisDTO analysisDTO = CropAnalysisDTO.builder()
                .userId(userId)
                .regionId(regionDTO.getRegionId())
                .recommendedCropId(cropDTO.getCropId())
                .recommendedReason(CmmUtil.nvl(geminiDTO.getRecommendedReason()))
                .cultivationMethod(CmmUtil.nvl(geminiDTO.getCultivationMethod()))
                .riskSummary(CmmUtil.nvl(geminiDTO.getRiskSummary()))
                .weatherSummary(CmmUtil.nvl(weatherDTO.getWeatherSummary()))
                .score(geminiDTO.getScore())
                .build();

        int res = cropAnalysisMapper.insertCropAnalysis(analysisDTO);

        if (res < 1) {
            rMap.put("result", "ANALYSIS_SAVE_FAIL");
            rMap.put("msg", "작물 분석 결과 저장에 실패했습니다.");
            return rMap;
        }

        Long analysisId = analysisDTO.getAnalysisId();

        if (analysisId == null) {
            rMap.put("result", "ANALYSIS_ID_NOT_FOUND");
            rMap.put("msg", "분석 결과 저장은 완료되었지만 분석 번호를 가져오지 못했습니다.");
            return rMap;
        }

        rMap.put("result", "LOADING_OK");
        rMap.put("msg", "작물 분석이 완료되었습니다.");
        rMap.put("analysisId", analysisId);
        rMap.put("regionId", regionDTO.getRegionId());
        rMap.put("fullRegionName", regionDTO.getFullRegionName());

        log.info("{}.cropLoadingProc End!", this.getClass().getName());

        return rMap;
    }

}