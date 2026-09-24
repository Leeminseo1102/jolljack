package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.*;
import kopo.poly.jolljack.service.ITradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {

    private final ITradeService tradeService;

    @GetMapping("/view")
    public String tradePage(TradeSearchDTO pDTO, Model model, HttpSession session) {

        log.info("{}.tradePage Start!",
                this.getClass().getName());

        try {

            Long userId =
                    (Long) session.getAttribute("userId");


            log.info("거래 페이지 진입 userId : {}, sidoName : {}, page : {}",
                    userId,
                    pDTO.sidoName(),
                    pDTO.page());


            String sidoName =
                    pDTO.sidoName();


            if (sidoName == null || sidoName.isBlank()) {

                sidoName =
                        tradeService.getUserSidoName(userId);


                log.info("거래 페이지 기본 시도 : {}",
                        sidoName);
            }

            TradeSearchDTO searchDTO = TradeSearchDTO.builder()
                    .sidoName(sidoName)
                    .page(pDTO.page())
                    .build();

            TradePageDTO rDTO =
                    tradeService.getTradePostList(
                            userId,
                            searchDTO
                    );

            List<RegionDTO> sidoList =
                    tradeService.getSidoList();

            model.addAttribute(
                    "trade",
                    rDTO
            );

            model.addAttribute(
                    "sidoList",
                    sidoList
            );

            model.addAttribute(
                    "currentSidoName",
                    sidoName
            );

            log.info("거래 페이지 Model currentSidoName : {}, totalCount : {}",
                    sidoName,
                    rDTO.totalCount());


            log.info("{}.tradePage End!",
                    this.getClass().getName());


            return "trade/trade";


        } catch (Exception e) {

            log.info("{}.tradePage Error : {}",
                    this.getClass().getName(),
                    e.getMessage(),
                    e);


            return "redirect:/main/view";
        }
    }


    @GetMapping("/getTradePostList")
    @ResponseBody
    public TradePageDTO getTradePostList(TradeSearchDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.getTradePostList Start!",
                this.getClass().getName());


        try {

            Long userId =
                    (Long) session.getAttribute("userId");


            log.info("거래 AJAX 요청 userId : {}, sidoName : {}, page : {}",
                    userId,
                    pDTO.sidoName(),
                    pDTO.page());


            TradePageDTO rDTO =
                    tradeService.getTradePostList(
                            userId,
                            pDTO
                    );


            log.info("거래 AJAX 응답 totalCount : {}, page : {}, totalPages : {}",
                    rDTO.totalCount(),
                    rDTO.page(),
                    rDTO.totalPages());


            log.info("{}.getTradePostList End!",
                    this.getClass().getName());


            return rDTO;


        } catch (Exception e) {

            log.info("{}.getTradePostList Error : {}",
                    this.getClass().getName(),
                    e.getMessage(),
                    e);


            throw e;
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {

        log.info("{}.registerPage Start!", this.getClass().getName());

        try {

            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return "redirect:/login/login";
            }

            List<RegionDTO> sidoList = tradeService.getSidoList();

            model.addAttribute("sidoList", sidoList);

            log.info("{}.registerPage End!", this.getClass().getName());

            return "trade/register";

        } catch (Exception e) {

            log.info("{}.registerPage Error : {}",
                    this.getClass().getName(), e.getMessage(), e);

            return "redirect:/trade/view";
        }
    }

    @GetMapping("/getSigunguList")
    @ResponseBody
    public List<RegionDTO> getSigunguList(@RequestParam String sidoName) throws Exception {

        log.info("{}.getSigunguList Start!", this.getClass().getName());

        try {

            RegionDTO pDTO = RegionDTO.builder()
                    .sidoName(sidoName)
                    .build();

            List<RegionDTO> rList = tradeService.getSigunguList(pDTO);

            log.info("{}.getSigunguList End!", this.getClass().getName());

            return rList;

        } catch (Exception e) {

            log.info("{}.getSigunguList Error : {}",
                    this.getClass().getName(), e.getMessage(), e);

            throw e;
        }
    }

    @PostMapping("/registerProc")
    public String registerProc(@ModelAttribute TradePostRegisterDTO pDTO, HttpSession session) {

        log.info("{}.registerProc Start!", this.getClass().getName());

        try {

            Long userId = (Long) session.getAttribute("userId");

            String sidoName = tradeService.registerTradePost(userId, pDTO);

            String encodedSidoName = URLEncoder.encode(
                    sidoName,
                    StandardCharsets.UTF_8
            );

            log.info("{}.registerProc End!", this.getClass().getName());

            return "redirect:/trade/view?sidoName=" + encodedSidoName;

        } catch (Exception e) {

            log.info("{}.registerProc Error : {}",
                    this.getClass().getName(), e.getMessage(), e);

            return "redirect:/trade/register";
        }
    }

    @ResponseBody
    @GetMapping("/getTradePostDetail")
    public TradePostDetailDTO getTradePostDetail(@RequestParam Long tradePostId, HttpSession session) throws Exception {

        log.info("{}.getTradePostDetail Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        log.info("거래 상세 조회 요청 userId : {}, tradePostId : {}",
                userId,
                tradePostId);

        TradePostDetailDTO pDTO = TradePostDetailDTO.builder()
                .tradePostId(tradePostId)
                .build();

        TradePostDetailDTO rDTO =
                tradeService.getTradePostDetail(userId, pDTO);

        log.info("{}.getTradePostDetail End!", this.getClass().getName());

        return rDTO;
    }
}