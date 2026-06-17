package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.DiagUploadDTO;
import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;
import kopo.poly.jolljack.dto.ImgDTO;
import kopo.poly.jolljack.dto.ImgGeminiDTO;
import kopo.poly.jolljack.service.IDiagLoadingService;
import kopo.poly.jolljack.service.IDiagResultService;
import kopo.poly.jolljack.service.IImgService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/diag")
public class DiagController {

    private  final IDiagLoadingService diagLoadingService;
    private final IImgService imgService;
    private final IDiagResultService diagResultService;

    /**
     * 병충해 이미지 분석 화면
     */
    @GetMapping("/ImgTrans")
    public String imgTransPage() {

        log.info("{}.imgTransPage Start!", this.getClass().getName());

        try {
            log.info("{}.imgTransPage End!", this.getClass().getName());

            return "diag/ImgTrans";

        } catch (Exception e) {
            log.info("{}.imgTransPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/main/view";
        }
    }

    /**
     * 병충해 이미지 업로드 처리
     */
    @PostMapping("/uploadProc")
    @ResponseBody
    public Map<String, Object> uploadProc(@ModelAttribute ImgDTO pDTO, HttpSession session) {

        log.info("{}.uploadProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            DiagUploadDTO uploadDTO = imgService.uploadDiagImageProc(pDTO);

            session.setAttribute("DIAG_UPLOAD_DTO", uploadDTO);

            log.info("image mimeType : {}", uploadDTO.mimeType());
            log.info("S3 image uploaded. uploadDTO saved in session.");

            rMap.put("result", "UPLOAD_OK");
            rMap.put("msg", "이미지 업로드가 완료되었습니다.");

        } catch (Exception e) {
            log.info("{}.uploadProc Error : {}", this.getClass().getName(), e.getMessage(), e);

            rMap.put("result", "FAIL");
            rMap.put("msg", e.getMessage());
        }

        log.info("{}.uploadProc End!", this.getClass().getName());

        return rMap;
    }

    /**
     * 병충해 진단 로딩 화면
     */
    @GetMapping("/loading")
    public String loadingPage(HttpSession session) {

        log.info("{}.loadingPage Start!", this.getClass().getName());

        try {

            if (session.getAttribute("userId") == null) {
                return "redirect:/login/form";
            }

            if (session.getAttribute("DIAG_UPLOAD_DTO") == null) {
                return "redirect:/diag/ImgTrans";
            }

            log.info("{}.loadingPage End!", this.getClass().getName());

            return "diag/loading";

        } catch (Exception e) {
            log.info("{}.loadingPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/diag/ImgTrans";
        }
    }

    /**
     * 병충해 진단 로딩 처리
     */
    @PostMapping("/loadingProc")
    @ResponseBody
    public Map<String, Object> loadingProc(HttpSession session) {

        log.info("{}.loadingProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {

            rMap = diagLoadingService.diagLoadingProc(session);

        } catch (Exception e) {
            log.info("{}.loadingProc Error : {}", this.getClass().getName(), e.getMessage(), e);

            rMap.put("result", "FAIL");
            rMap.put("msg", "병충해 진단 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.loadingProc End!", this.getClass().getName());

        return rMap;
    }

    /**
     * 병충해 진단 결과 화면
     */
    @GetMapping("/result")
    public String resultPage(HttpSession session) {

        log.info("{}.resultPage Start!", this.getClass().getName());

        try {

            if (session.getAttribute("userId") == null) {
                return "redirect:/login/form";
            }

            log.info("{}.resultPage End!", this.getClass().getName());

            return "diag/result";

        } catch (Exception e) {
            log.info("{}.resultPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/diag/ImgTrans";
        }
    }

    /**
     * 병충해 진단 결과 데이터 조회
     */
    @PostMapping("/resultData")
    @ResponseBody
    public Map<String, Object> diagResultData(HttpServletRequest request, HttpSession session) {

        log.info("{}.diagResultData Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {

            String diagnosisIdStr = CmmUtil.nvl(request.getParameter("diagnosisId"));

            Long diagnosisId = null;

            if (!diagnosisIdStr.isEmpty()) {
                diagnosisId = Long.parseLong(diagnosisIdStr);
            }

            DiseaseDiagnosisDTO pDTO = DiseaseDiagnosisDTO.builder()
                    .diagnosisId(diagnosisId)
                    .build();

            rMap = diagResultService.getDiagResultDataProc(pDTO, session);

        } catch (Exception e) {
            log.info("{}.diagResultData Error : {}", this.getClass().getName(), e.getMessage(), e);

            rMap.put("result", "FAIL");
            rMap.put("msg", "진단 결과 조회 중 오류가 발생했습니다.");
        }

        log.info("{}.diagResultData End!", this.getClass().getName());

        return rMap;
    }
}