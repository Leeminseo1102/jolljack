package kopo.poly.jolljack.controller;

import kopo.poly.jolljack.dto.FarmDicDetailDTO;
import kopo.poly.jolljack.dto.FarmDicSearchDTO;
import kopo.poly.jolljack.service.IFarmDicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/farmDic")
public class FarmDicController {

    private final IFarmDicService farmDicService;


    @GetMapping("/view")
    public String view() {

        log.info("{}.view Start!", this.getClass().getName());

        log.info("{}.view End!", this.getClass().getName());

        return "farmDic/dict";
    }

    @ResponseBody
    @GetMapping("/search")
    public List<FarmDicSearchDTO> searchFrontMatch(@RequestParam String word, @RequestParam(defaultValue = "1") int pageNo) throws Exception {

        log.info("{}.searchFrontMatch Start!", this.getClass().getName());

        List<FarmDicSearchDTO> rList =
                farmDicService.searchFrontMatch(
                        word,
                        pageNo
                );

        log.info("{}.searchFrontMatch End!", this.getClass().getName());

        return rList;
    }


    @ResponseBody
    @GetMapping("/detail")
    public FarmDicDetailDTO detailWord(@RequestParam String wordNo) throws Exception {

        log.info("{}.detailWord Start!", this.getClass().getName());

        FarmDicDetailDTO rDTO =
                farmDicService.detailWord(wordNo);

        log.info("{}.detailWord End!", this.getClass().getName());

        return rDTO;
    }
}