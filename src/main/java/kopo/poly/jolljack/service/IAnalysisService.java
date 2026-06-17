package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.RegionDTO;

import java.util.List;
import java.util.Map;

public interface IAnalysisService {

    List<RegionDTO> getSidoList() throws Exception;

    List<RegionDTO> getSigunguListProc(HttpServletRequest request) throws Exception;

    Map<String, Object> selectRegionProc(HttpServletRequest request, HttpSession session) throws Exception;
}