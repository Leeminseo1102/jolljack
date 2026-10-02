package kopo.poly.jolljack.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.TradeMessageDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomListDTO;
import kopo.poly.jolljack.service.ISseService;
import kopo.poly.jolljack.service.ITradeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/trade/chat")
public class TradeMessageController {

    private final ITradeMessageService tradeMessageService;

    private final ISseService sseService;


    @ResponseBody
    @PostMapping("/room")
    public TradeMessageRoomDTO getOrCreateTradeMessageRoom(@RequestParam Long tradePostId, HttpSession session) throws Exception {

        log.info("{}.getOrCreateTradeMessageRoom Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradeMessageRoomDTO pDTO = TradeMessageRoomDTO.builder()
                .tradePostId(tradePostId)
                .build();

        TradeMessageRoomDTO rDTO =
                tradeMessageService.getOrCreateTradeMessageRoom(userId, pDTO);

        log.info("{}.getOrCreateTradeMessageRoom End!", this.getClass().getName());

        return rDTO;
    }


    @ResponseBody
    @GetMapping("/messages")
    public List<TradeMessageDTO> getTradeMessageList(@RequestParam Long messageRoomId, HttpSession session) throws Exception {

        log.info("{}.getTradeMessageList Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradeMessageDTO pDTO = TradeMessageDTO.builder()
                .messageRoomId(messageRoomId)
                .build();

        List<TradeMessageDTO> rList =
                tradeMessageService.getTradeMessageList(userId, pDTO);

        log.info("{}.getTradeMessageList End!", this.getClass().getName());

        return rList;
    }


    @ResponseBody
    @PostMapping("/send")
    public TradeMessageDTO sendTradeMessage(@RequestParam Long messageRoomId,
                                            @RequestParam String messageText,
                                            HttpSession session) throws Exception {

        log.info("{}.sendTradeMessage Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradeMessageDTO pDTO = TradeMessageDTO.builder()
                .messageRoomId(messageRoomId)
                .messageText(messageText)
                .build();

        TradeMessageDTO rDTO =
                tradeMessageService.sendTradeMessage(userId, pDTO);

        sseService.sendMessage(
                messageRoomId,
                rDTO
        );

        log.info("{}.sendTradeMessage End!", this.getClass().getName());

        return rDTO;
    }


    @ResponseBody
    @PostMapping("/read")
    public int readTradeMessage(@RequestParam Long messageRoomId, HttpSession session) throws Exception {

        log.info("{}.readTradeMessage Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        TradeMessageDTO pDTO = TradeMessageDTO.builder()
                .messageRoomId(messageRoomId)
                .build();

        int res =
                tradeMessageService.readTradeMessage(userId, pDTO);

        if (res > 0) {

            sseService.sendRead(
                    messageRoomId
            );
        }

        log.info("{}.readTradeMessage End!", this.getClass().getName());

        return res;
    }


    @ResponseBody
    @GetMapping("/rooms")
    public List<TradeMessageRoomListDTO> getTradeMessageRoomList(HttpSession session) throws Exception {

        log.info("{}.getTradeMessageRoomList Start!", this.getClass().getName());

        Long userId =
                (Long) session.getAttribute("userId");

        List<TradeMessageRoomListDTO> rList =
                tradeMessageService.getTradeMessageRoomList(userId);

        log.info("{}.getTradeMessageRoomList End!", this.getClass().getName());

        return rList;
    }
}