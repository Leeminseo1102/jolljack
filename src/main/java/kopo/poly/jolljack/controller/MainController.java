package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {

    /**
     * 메인 페이지
     */
    @GetMapping("/view")
    public String mainPage(HttpSession session) {

        log.info("{}.mainPage Start!", this.getClass().getName());

        try {
            Long userId = (Long) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");

            log.info("session userId : {}", userId);
            log.info("session userName : {}", userName);

            log.info("{}.mainPage End!", this.getClass().getName());

            return "main/main";

        } catch (Exception e) {
            log.info("{}.mainPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }
}