package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.DiDiDTO;
import kopo.poly.jolljack.dto.DiagUploadDTO;
import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;
import kopo.poly.jolljack.dto.GeminiReDiagDTO;
import kopo.poly.jolljack.dto.ImgGeminiDTO;
import kopo.poly.jolljack.dto.PapCandidateDTO;
import kopo.poly.jolljack.mapper.IDiseaseDiagnosisMapper;
import kopo.poly.jolljack.service.IDiagLoadingService;
import kopo.poly.jolljack.service.IGeminiService;
import kopo.poly.jolljack.service.IImgService;
import kopo.poly.jolljack.service.IPAPService;
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
public class DiagLoadingService implements IDiagLoadingService {

    private final IImgService imgService;
    private final IGeminiService geminiService;
    private final IPAPService papService;
    private final IDiseaseDiagnosisMapper diseaseDiagnosisMapper;

    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_DIAG_UPLOAD_DTO = "DIAG_UPLOAD_DTO";

    @Override
    public Map<String, Object> diagLoadingProc(HttpSession session) throws Exception {

        log.info("{}.diagLoadingProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);

        if (userId == null) {
            rMap.put("result", "LOGIN_REQUIRED");
            rMap.put("msg", "로그인 후 이용 가능합니다.");
            return rMap;
        }

        DiagUploadDTO uploadDTO = (DiagUploadDTO) session.getAttribute(SESSION_DIAG_UPLOAD_DTO);

        if (uploadDTO == null) {
            rMap.put("result", "UPLOAD_NOT_FOUND");
            rMap.put("msg", "업로드된 이미지 정보가 없습니다.");
            return rMap;
        }

        ImgGeminiDTO imgGeminiDTO = imgService.makeImgGeminiDTO(uploadDTO);

        GeminiReDiagDTO featureDTO = geminiService.getDiagFeature(imgGeminiDTO);

        List<PapCandidateDTO> top3List = papService.getTop3CandidatesProc(featureDTO);

        DiDiDTO finalDTO = geminiService.getDiagFinalResultProc(featureDTO, top3List);

        DiseaseDiagnosisDTO saveDTO = DiseaseDiagnosisDTO.builder()
                .userId(userId)
                .predictedDiseaseName(CmmUtil.nvl(finalDTO.predictedDiseaseName()))
                .diseaseDescription(CmmUtil.nvl(finalDTO.diseaseDescription()))
                .solutionText(CmmUtil.nvl(finalDTO.solutionText()))
                .symptomSummary(CmmUtil.nvl(finalDTO.symptomSummary()))
                .preventionText(CmmUtil.nvl(finalDTO.preventionText()))
                .build();

        int res = diseaseDiagnosisMapper.insertDiseaseDiagnosis(saveDTO);

        if (res < 1) {
            rMap.put("result", "DIAG_SAVE_FAIL");
            rMap.put("msg", "병충해 진단 결과 저장에 실패했습니다.");
            return rMap;
        }

        Long diagnosisId = saveDTO.getDiagnosisId();

        if (diagnosisId == null) {
            rMap.put("result", "DIAG_ID_NOT_FOUND");
            rMap.put("msg", "진단 결과 저장은 완료되었지만 진단 번호를 가져오지 못했습니다.");
            return rMap;
        }

        session.removeAttribute(SESSION_DIAG_UPLOAD_DTO);

        rMap.put("result", "LOADING_OK");
        rMap.put("msg", "병충해 진단이 완료되었습니다.");
        rMap.put("diagnosisId", diagnosisId);

        log.info("diagnosisId : {}", diagnosisId);
        log.info("{}.diagLoadingProc End!", this.getClass().getName());

        return rMap;
    }

}