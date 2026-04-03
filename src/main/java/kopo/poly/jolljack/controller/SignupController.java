package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.service.ISignupService;
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
@RequestMapping("/signup")
public class SignupController {

    private final ISignupService signupService;

    @GetMapping("/step1")
    public String signupStep1() {

        log.info("{}.signupStep1 Start!", this.getClass().getName());

        try {
            log.info("{}.signupStep1 End!", this.getClass().getName());
            return "signup/step1";

        } catch (Exception e) {
            log.info("{}.signupStep1 Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }

    @PostMapping("/sendEmailCode")
    @ResponseBody
    public Map<String, Object> sendEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.sendEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = signupService.sendEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.sendEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "인증코드 발송 중 오류가 발생했습니다.");
        }

        log.info("{}.sendEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @PostMapping("/verifyEmailCode")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = signupService.verifyEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.verifyEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "이메일 인증 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/step2")
    public String signupStep2(HttpSession session) {

        log.info("{}.signupStep2 Start!", this.getClass().getName());

        try {
            String result = signupService.checkStep1Session(session);

            if (!"STEP1_OK".equals(result)) {
                log.info("{}.signupStep2 End!", this.getClass().getName());
                return "redirect:/signup/step1";
            }

            log.info("{}.signupStep2 End!", this.getClass().getName());
            return "signup/step2";

        } catch (Exception e) {
            log.info("{}.signupStep2 Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/signup/step1";
        }
    }

    @PostMapping("/checkLoginId")
    @ResponseBody
    public Map<String, Object> checkLoginId(HttpServletRequest request) {

        log.info("{}.checkLoginId Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = signupService.checkLoginIdProc(request);

        } catch (Exception e) {
            log.info("{}.checkLoginId Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "아이디 확인 중 오류가 발생했습니다.");
        }

        log.info("{}.checkLoginId End!", this.getClass().getName());

        return rMap;
    }

    @PostMapping("/saveStep2")
    @ResponseBody
    public Map<String, Object> saveStep2(HttpServletRequest request, HttpSession session) {

        log.info("{}.saveStep2 Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = signupService.saveStep2Proc(request, session);

        } catch (Exception e) {
            log.info("{}.saveStep2 Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "회원가입 2단계 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.saveStep2 End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/step3")
    public String signupStep3(HttpSession session, Model model) {

        log.info("{}.signupStep3 Start!", this.getClass().getName());

        try {
            String result = signupService.checkStep2Session(session);

            if (!"STEP2_OK".equals(result)) {
                log.info("{}.signupStep3 End!", this.getClass().getName());
                return "redirect:/signup/step1";
            }

            List<RegionDTO> sidoList = signupService.getSidoList();
            List<CropDTO> cropList = signupService.getCropList();

            model.addAttribute("sidoList", sidoList);
            model.addAttribute("cropList", cropList);

            log.info("{}.signupStep3 End!", this.getClass().getName());

            return "signup/step3";

        } catch (Exception e) {
            log.info("{}.signupStep3 Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/signup/step1";
        }
    }

    @GetMapping("/getSigungu")
    @ResponseBody
    public List<RegionDTO> getSigungu(HttpServletRequest request) throws Exception {

        log.info("{}.getSigungu Start!", this.getClass().getName());

        List<RegionDTO> rList = signupService.getSigunguListProc(request);

        log.info("{}.getSigungu End!", this.getClass().getName());

        return rList;
    }

    @PostMapping("/complete")
    @ResponseBody
    public Map<String, Object> signupComplete(HttpServletRequest request, HttpSession session) {

        log.info("{}.signupComplete Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = signupService.signupCompleteProc(request, session);

        } catch (Exception e) {
            log.info("{}.signupComplete Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "회원가입 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.signupComplete End!", this.getClass().getName());

        return rMap;
    }
}