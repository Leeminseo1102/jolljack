package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.service.ISseService;
import kopo.poly.jolljack.service.ITradeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final ISseService sseService;

    private final ITradeMessageService tradeMessageService;

    @ResponseBody
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam Long messageRoomId, HttpSession session) throws Exception {

        log.info("{}.connect Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        tradeMessageService.validateTradeMessageRoom(
                userId,
                messageRoomId
        );

        SseEmitter rEmitter =
                sseService.connect(messageRoomId);

        log.info("SSE 연결 userId : {}, messageRoomId : {}",
                userId,
                messageRoomId);

        log.info("{}.connect End!", this.getClass().getName());

        return rEmitter;
    }
}