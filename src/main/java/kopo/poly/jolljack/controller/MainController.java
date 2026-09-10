package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.RegionWeatherDTO;
import kopo.poly.jolljack.service.IRegionWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {

    private final IRegionWeatherService regionWeatherService;

    /**
     * 메인 페이지
     */
    @GetMapping("/view")
    public String mainPage(HttpSession session, Model model) {

        log.info("{}.mainPage Start!", this.getClass().getName());

        try {
            Long userId = (Long) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");

            log.info("session userId : {}", userId);
            log.info("session userName : {}", userName);

            if (userId != null) {

                try {
                    RegionWeatherDTO rDTO =
                            regionWeatherService.getRegionWeatherProc(userId);

                    model.addAttribute("weather", rDTO);

                } catch (Exception e) {
                    log.info("{}.mainPage Weather Error : {}",
                            this.getClass().getName(), e.getMessage(), e);

                    model.addAttribute("weather", null);
                }
            }
            log.info("{}.mainPage End!", this.getClass().getName());

            return "main/main";

        } catch (Exception e) {
            log.info("{}.mainPage Error : {}", this.getClass().getName(), e.getMessage(), e);
            return "redirect:/";
        }
    }
}