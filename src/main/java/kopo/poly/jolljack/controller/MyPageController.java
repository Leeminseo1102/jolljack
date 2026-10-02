package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.TradePostDetailDTO;
import kopo.poly.jolljack.dto.TradePostListDTO;
import kopo.poly.jolljack.service.IMyPageService;
import kopo.poly.jolljack.service.ISignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Controller
public class MyPageController {

    private final IMyPageService myPageService;
    private final ISignupService signupService;

    /**
     * 마이페이지 화면
     */
    @GetMapping("/view")
    public String myPageView(HttpSession session, ModelMap model) throws Exception {

        log.info("{}.myPageView Start!", this.getClass().getName());

        Map<String, Object> rMap = myPageService.getMyPageInfoProc(session);

        String result = String.valueOf(rMap.get("result"));

        if ("login".equals(result)) {
            return "redirect:/login/form";
        }

        if (!"success".equals(result)) {
            model.addAttribute("msg", rMap.get("msg"));
            return "redirect:/main/view";
        }

        model.addAttribute("userInfo", rMap.get("userInfo"));
        model.addAttribute("cropList", rMap.get("cropList"));
        model.addAttribute("analysisList", rMap.get("analysisList"));
        model.addAttribute("diagnosisList", rMap.get("diagnosisList"));
        model.addAttribute("sidoList", signupService.getSidoList());

        log.info("{}.myPageView End!", this.getClass().getName());

        return "mypage/mypage";
    }

    //시군구
    @GetMapping("/getSigungu")
    @ResponseBody
    public List<RegionDTO> getSigungu(HttpServletRequest request) throws Exception {

        log.info("{}.getSigungu Start!", this.getClass().getName());

        List<RegionDTO> rList = signupService.getSigunguListProc(request);

        log.info("{}.getSigungu End!", this.getClass().getName());

        return rList;
    }


    /**
     * 선호 작물 수정
     */
    @ResponseBody
    @PostMapping("/updateFavoriteCrop")
    public Map<String, Object> updateFavoriteCrop(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateFavoriteCrop Start!", this.getClass().getName());

        Map<String, Object> rMap = myPageService.updateFavoriteCropProc(request, session);

        log.info("{}.updateFavoriteCrop End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 지역 수정
     */
    @ResponseBody
    @PostMapping("/updateRegion")
    public Map<String, Object> updateRegion(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateRegion Start!", this.getClass().getName());

        Map<String, Object> rMap = myPageService.updateRegionProc(request, session);

        log.info("{}.updateRegion End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 회원탈퇴
     */
    @ResponseBody
    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(HttpSession session) throws Exception {

        log.info("{}.withdraw Start!", this.getClass().getName());

        Map<String, Object> rMap = myPageService.withdrawUserProc(session);

        log.info("{}.withdraw End!", this.getClass().getName());

        return rMap;
    }

    //분석
    @GetMapping("/analysis/result/{analysisId}")
    public String myPageAnalysisResult(@PathVariable Long analysisId, HttpSession session, ModelMap model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login/form";
        }

        model.addAttribute("analysisId", analysisId);
        model.addAttribute("reportMode", true);

        return "crop/result";
    }

    @GetMapping("/analysis/risk/{analysisId}")
    public String myPageAnalysisRisk(@PathVariable Long analysisId, HttpSession session, ModelMap model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login/form";
        }

        model.addAttribute("analysisId", analysisId);
        model.addAttribute("reportMode", true);

        return "crop/risk";
    }

    //진단
    @GetMapping("/diagnosis/result/{diagnosisId}")
    public String myPageDiagnosisResult(@PathVariable Long diagnosisId, HttpSession session, ModelMap model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login/form";
        }

        model.addAttribute("diagnosisId", diagnosisId);
        model.addAttribute("reportMode", true);

        return "diag/result";
    }

    /**
     * 관심 거래글 조회
     */
    @ResponseBody
    @GetMapping("/getFavoriteTradeList")
    public List<TradePostListDTO> getFavoriteTradeList(HttpSession session) throws Exception {

        log.info("{}.getFavoriteTradeList Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        List<TradePostListDTO> rList =
                myPageService.getFavoriteTradeListProc(userId);

        log.info("{}.getFavoriteTradeList End!", this.getClass().getName());

        return rList;
    }

    /**
     * 내가 등록한 거래글 조회
     */
    @ResponseBody
    @GetMapping("/getMyTradeList")
    public List<TradePostListDTO> getMyTradeList(HttpSession session) throws Exception {

        log.info("{}.getMyTradeList Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        List<TradePostListDTO> rList =
                myPageService.getMyTradeListProc(userId);

        log.info("{}.getMyTradeList End!", this.getClass().getName());

        return rList;
    }


    /**
     * 내가 등록한 거래글 상태 변경
     */
    @ResponseBody
    @PostMapping("/updateMyTradeStatus")
    public Map<String, Object> updateMyTradeStatus(@RequestParam Long tradePostId,
                                                   @RequestParam String status,
                                                   HttpSession session) throws Exception {

        log.info("{}.updateMyTradeStatus Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradePostDetailDTO pDTO = TradePostDetailDTO.builder()
                .tradePostId(tradePostId)
                .status(status)
                .build();

        Map<String, Object> rMap =
                myPageService.updateMyTradeStatusProc(userId, pDTO);

        log.info("{}.updateMyTradeStatus End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 내가 등록한 거래글 삭제
     */
    @ResponseBody
    @PostMapping("/deleteMyTradePost")
    public Map<String, Object> deleteMyTradePost(@RequestParam Long tradePostId,
                                                 HttpSession session) throws Exception {

        log.info("{}.deleteMyTradePost Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradePostDetailDTO pDTO = TradePostDetailDTO.builder()
                .tradePostId(tradePostId)
                .build();

        Map<String, Object> rMap =
                myPageService.deleteMyTradePostProc(userId, pDTO);

        log.info("{}.deleteMyTradePost End!", this.getClass().getName());

        return rMap;
    }


}