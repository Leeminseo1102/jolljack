package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.mapper.ISignupMapper;
import kopo.poly.jolljack.service.IAnalysisService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService implements IAnalysisService {

    private final ISignupMapper signupMapper;

    private static final String SESSION_ANALYSIS_REGION_ID = "analysisRegionId";

    @Override
    public List<RegionDTO> getSidoList() throws Exception {

        log.info("{}.getSidoList Start!", this.getClass().getName());

        List<RegionDTO> rList = signupMapper.getSidoList();

        log.info("{}.getSidoList End!", this.getClass().getName());

        return rList;
    }

    @Override
    public List<RegionDTO> getSigunguListProc(HttpServletRequest request) throws Exception {

        log.info("{}.getSigunguListProc Start!", this.getClass().getName());

        String sidoName = CmmUtil.nvl(request.getParameter("sidoName"));

        RegionDTO pDTO = RegionDTO.builder()
                .sidoName(sidoName)
                .build();

        List<RegionDTO> rList = signupMapper.getSigunguList(pDTO);

        log.info("{}.getSigunguListProc End!", this.getClass().getName());

        return rList;
    }

    @Override
    public Map<String, Object> selectRegionProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.selectRegionProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        String sidoName = CmmUtil.nvl(request.getParameter("sidoName"));
        String sigunguName = CmmUtil.nvl(request.getParameter("sigunguName"));

        if (sidoName.isEmpty()) {
            rMap.put("result", "SIDO_EMPTY");
            rMap.put("msg", getMsg("SIDO_EMPTY"));
            return rMap;
        }

        if (sigunguName.isEmpty()) {
            rMap.put("result", "SIGUNGU_EMPTY");
            rMap.put("msg", getMsg("SIGUNGU_EMPTY"));
            return rMap;
        }

        RegionDTO pDTO = RegionDTO.builder()
                .sidoName(sidoName)
                .sigunguName(sigunguName)
                .build();

        RegionDTO regionDTO = signupMapper.getRegionId(pDTO);

        if (regionDTO == null || regionDTO.getRegionId() == null) {
            rMap.put("result", "REGION_NOT_FOUND");
            rMap.put("msg", getMsg("REGION_NOT_FOUND"));
            return rMap;
        }

        session.setAttribute(SESSION_ANALYSIS_REGION_ID, regionDTO.getRegionId());

        rMap.put("result", "SELECT_OK");
        rMap.put("msg", getMsg("SELECT_OK"));
        rMap.put("regionId", regionDTO.getRegionId());
        rMap.put("fullRegionName", CmmUtil.nvl(regionDTO.getFullRegionName()));

        log.info("{}.selectRegionProc End!", this.getClass().getName());

        return rMap;
    }

    private String getMsg(String code) {
        return switch (code) {
            case "SIDO_EMPTY" -> "시/도를 선택해주세요.";
            case "SIGUNGU_EMPTY" -> "시/군/구를 선택해주세요.";
            case "REGION_NOT_FOUND" -> "선택한 지역 정보를 찾을 수 없습니다.";
            case "SELECT_OK" -> "지역 선택이 완료되었습니다.";
            default -> "오류가 발생했습니다.";
        };
    }
}