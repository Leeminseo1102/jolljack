package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.TradeMessageDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISseService {

    SseEmitter connect(Long messageRoomId) throws Exception;

    void sendMessage(Long messageRoomId, TradeMessageDTO pDTO);

    void sendRead(Long messageRoomId);
}