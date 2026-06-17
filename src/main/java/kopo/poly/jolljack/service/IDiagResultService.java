package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;

import java.util.Map;

public interface IDiagResultService {

    Map<String, Object> getDiagResultDataProc(DiseaseDiagnosisDTO pDTO, HttpSession session) throws Exception;

}