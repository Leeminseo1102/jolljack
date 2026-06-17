package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropAnalysisDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.service.IAnalysisService;
import kopo.poly.jolljack.service.ICropLoadingService;
import kopo.poly.jolljack.service.IMapResultService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/crop")
public class CropController {

    private final IAnalysisService analysisService;
    private final ICropLoadingService cropLoadingService;
    private final IMapResultService mapResultService;

    @GetMapping("/region")
    public String analysisRegionPage(Model model) {

        log.info("{}.analysisRegionPage Start!", this.getClass().getName());

        try {
            List<RegionDTO> sidoList = analysisService.getSidoList();

            model.addAttribute("sidoList", sidoList);

            log.info("{}.analysisRegionPage End!", this.getClass().getName());

            return "crop/region";

        } catch (Exception e) {
            log.info("{}.analysisRegionPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/main/view";
        }
    }

    @GetMapping("/getSigungu")
    @ResponseBody
    public List<RegionDTO> getSigungu(HttpServletRequest request) throws Exception {

        log.info("{}.getSigungu Start!", this.getClass().getName());

        List<RegionDTO> rList = analysisService.getSigunguListProc(request);

        log.info("{}.getSigungu End!", this.getClass().getName());

        return rList;
    }

    @PostMapping("/selectRegion")
    @ResponseBody
    public Map<String, Object> selectRegion(HttpServletRequest request, HttpSession session) {

        log.info("{}.selectRegion Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = analysisService.selectRegionProc(request, session);

        } catch (Exception e) {
            log.info("{}.selectRegion Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "지역 선택 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.selectRegion End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/loading")
    public String cropLoadingPage(HttpSession session) {

        log.info("{}.cropLoadingPage Start!", this.getClass().getName());

        try {
            Long regionId = (Long) session.getAttribute("analysisRegionId");

            if (regionId == null) {
                log.info("{}.cropLoadingPage End!", this.getClass().getName());
                return "redirect:/crop/region";
            }

            log.info("{}.cropLoadingPage End!", this.getClass().getName());

            return "crop/loading";

        } catch (Exception e) {
            log.info("{}.cropLoadingPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/crop/region";
        }
    }

    @PostMapping("/loadingProc")
    @ResponseBody
    public Map<String, Object> cropLoadingProc(HttpSession session) {

        log.info("{}.cropLoadingProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = cropLoadingService.cropLoadingProc(session);

        } catch (Exception e) {
            log.info("{}.cropLoadingProc Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "작물 분석 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.cropLoadingProc End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/result")
    public String cropResultPage(HttpSession session) {

        log.info("{}.cropResultPage Start!", this.getClass().getName());

        try {
            Long regionId = (Long) session.getAttribute("analysisRegionId");

            if (regionId == null) {
                log.info("{}.cropResultPage End!", this.getClass().getName());
                return "redirect:/crop/region";
            }

            log.info("{}.cropResultPage End!", this.getClass().getName());

            return "crop/result";

        } catch (Exception e) {
            log.info("{}.cropResultPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/crop/region";
        }
    }

    @PostMapping("/resultData")
    @ResponseBody
    public Map<String, Object> cropResultData(HttpServletRequest request, HttpSession session) {

        log.info("{}.cropResultData Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {

            String analysisIdStr = CmmUtil.nvl(request.getParameter("analysisId"));

            Long analysisId = null;

            if (!analysisIdStr.isEmpty()) {
                analysisId = Long.parseLong(analysisIdStr);
            }

            CropAnalysisDTO pDTO = CropAnalysisDTO.builder()
                    .analysisId(analysisId)
                    .build();

            rMap = mapResultService.getResultDataProc(pDTO, session);

        } catch (Exception e) {
            log.info("{}.cropResultData Error : {}", this.getClass().getName(), e.getMessage(), e);

            rMap.put("result", "FAIL");
            rMap.put("msg", "결과 조회 중 오류가 발생했습니다.");
        }

        log.info("{}.cropResultData End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/risk")
    public String riskPage(HttpSession session) {

        log.info("{}.riskPage Start!", this.getClass().getName());

        try {
            Long regionId = (Long) session.getAttribute("analysisRegionId");

            if (regionId == null) {
                return "redirect:/crop/region";
            }

            log.info("{}.riskPage End!", this.getClass().getName());

            return "crop/risk";

        } catch (Exception e) {
            log.info("{}.riskPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/crop/region";
        }
    }
}