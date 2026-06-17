package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropAnalysisDTO;

import java.util.Map;

public interface IMapResultService {

    Map<String, Object> getResultDataProc(CropAnalysisDTO pDTO, HttpSession session) throws Exception;

}