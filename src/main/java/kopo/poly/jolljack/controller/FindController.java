package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.service.IFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/find")
public class FindController {

    private final IFindService findService;

    @GetMapping("/id")
    public String findIdPage() {

        log.info("{}.findIdPage Start!", this.getClass().getName());

        try {
            log.info("{}.findIdPage End!", this.getClass().getName());
            return "find/findId";

        } catch (Exception e) {
            log.info("{}.findIdPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }

    @PostMapping("/id/sendEmailCode")
    @ResponseBody
    public Map<String, Object> sendFindIdEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.sendFindIdEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.sendFindIdEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.sendFindIdEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "인증코드 발송 중 오류가 발생했습니다.");
        }

        log.info("{}.sendFindIdEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @PostMapping("/id/verifyEmailCode")
    @ResponseBody
    public Map<String, Object> verifyFindIdEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.verifyFindIdEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.verifyFindIdEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.verifyFindIdEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "이메일 인증 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.verifyFindIdEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @PostMapping("/id/result")
    @ResponseBody
    public Map<String, Object> findLoginId(HttpSession session) {

        log.info("{}.findLoginId Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.findLoginIdProc(session);

        } catch (Exception e) {
            log.info("{}.findLoginId Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "아이디 찾기 중 오류가 발생했습니다.");
        }

        log.info("{}.findLoginId End!", this.getClass().getName());

        return rMap;
    }

    // 비번=========================================================================================//

    @GetMapping("/password")
    public String findPasswordPage() {

        log.info("{}.findPasswordPage Start!", this.getClass().getName());

        try {
            log.info("{}.findPasswordPage End!", this.getClass().getName());
            return "find/findPassword";

        } catch (Exception e) {
            log.info("{}.findPasswordPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }

    @PostMapping("/password/sendEmailCode")
    @ResponseBody
    public Map<String, Object> sendFindPwEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.sendFindPwEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.sendFindPwEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.sendFindPwEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "인증코드 발송 중 오류가 발생했습니다.");
        }

        log.info("{}.sendFindPwEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @PostMapping("/password/verifyEmailCode")
    @ResponseBody
    public Map<String, Object> verifyFindPwEmailCode(HttpServletRequest request, HttpSession session) {

        log.info("{}.verifyFindPwEmailCode Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.verifyFindPwEmailCodeProc(request, session);

        } catch (Exception e) {
            log.info("{}.verifyFindPwEmailCode Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "이메일 인증 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.verifyFindPwEmailCode End!", this.getClass().getName());

        return rMap;
    }

    @GetMapping("/password/reset")
    public String resetPasswordPage(HttpSession session) {

        log.info("{}.resetPasswordPage Start!", this.getClass().getName());

        try {
            String result = findService.checkFindPwVerifiedSession(session);

            if (!"RESET_OK".equals(result)) {
                log.info("{}.resetPasswordPage End!", this.getClass().getName());
                return "redirect:/find/password";
            }

            log.info("{}.resetPasswordPage End!", this.getClass().getName());
            return "find/findPasswordReset";

        } catch (Exception e) {
            log.info("{}.resetPasswordPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/find/password";
        }
    }

    @PostMapping("/password/reset")
    @ResponseBody
    public Map<String, Object> resetPassword(HttpServletRequest request, HttpSession session) {

        log.info("{}.resetPassword Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = findService.resetPasswordProc(request, session);

        } catch (Exception e) {
            log.info("{}.resetPassword Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "비밀번호 재설정 중 오류가 발생했습니다.");
        }

        log.info("{}.resetPassword End!", this.getClass().getName());

        return rMap;
    }
}