package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;
import kopo.poly.jolljack.mapper.IDiseaseDiagnosisMapper;
import kopo.poly.jolljack.service.IDiagResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagResultService implements IDiagResultService {

    private final IDiseaseDiagnosisMapper diseaseDiagnosisMapper;

    @Override
    public Map<String, Object> getDiagResultDataProc(DiseaseDiagnosisDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.getDiagResultDataProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            rMap.put("result", "LOGIN_REQUIRED");
            rMap.put("msg", "로그인 후 이용 가능합니다.");
            return rMap;
        }

        if (pDTO == null || pDTO.getDiagnosisId() == null) {
            rMap.put("result", "DIAGNOSIS_ID_EMPTY");
            rMap.put("msg", "진단 번호가 없습니다.");
            return rMap;
        }

        DiseaseDiagnosisDTO searchDTO = DiseaseDiagnosisDTO.builder()
                .diagnosisId(pDTO.getDiagnosisId())
                .userId(userId)
                .build();

        DiseaseDiagnosisDTO rDTO = diseaseDiagnosisMapper.getDiseaseDiagnosisResult(searchDTO);

        if (rDTO == null) {
            rMap.put("result", "NOT_FOUND");
            rMap.put("msg", "진단 결과를 찾을 수 없습니다.");
            return rMap;
        }

        rMap.put("result", "SUCCESS");
        rMap.put("msg", "진단 결과 조회 성공");
        rMap.put("data", rDTO);

        log.info("diagnosisId : {}", rDTO.getDiagnosisId());
        log.info("{}.getDiagResultDataProc End!", this.getClass().getName());

        return rMap;
    }

}