package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.service.ILoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

    private final ILoginService loginService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/form")
    public String loginPage(HttpSession session) {

        log.info("{}.loginPage Start!", this.getClass().getName());

        try {
            Long userId = (Long) session.getAttribute("userId");

            if (userId != null) {
                log.info("{}.loginPage End!", this.getClass().getName());
                return "redirect:/main";
            }

            log.info("{}.loginPage End!", this.getClass().getName());
            return "login/login";

        } catch (Exception e) {
            log.info("{}.loginPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/proc")
    @ResponseBody
    public Map<String, Object> loginProc(HttpServletRequest request, HttpSession session) {

        log.info("{}.loginProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        try {
            rMap = loginService.loginProc(request, session);

        } catch (Exception e) {
            log.info("{}.loginProc Error : {}", this.getClass().getName(), e.getMessage(), e);
            rMap.put("result", "FAIL");
            rMap.put("msg", "로그인 처리 중 오류가 발생했습니다.");
        }

        log.info("{}.loginProc End!", this.getClass().getName());

        return rMap;
    }

    /**
     * 로그아웃
     */
    @GetMapping("/logout")
    public String logoutProc(HttpSession session) {

        log.info("{}.logoutProc Start!", this.getClass().getName());

        try {
            loginService.logoutProc(session);

            log.info("{}.logoutProc End!", this.getClass().getName());
            return "redirect:/main/view";

        } catch (Exception e) {
            log.info("{}.logoutProc Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }
}