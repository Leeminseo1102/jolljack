package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.TradeMessageDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomListDTO;

import java.util.List;

public interface ITradeMessageService {

    TradeMessageRoomDTO getOrCreateTradeMessageRoom(Long userId, TradeMessageRoomDTO pDTO) throws Exception;

    List<TradeMessageDTO> getTradeMessageList(Long userId, TradeMessageDTO pDTO) throws Exception;

    TradeMessageDTO sendTradeMessage(Long userId, TradeMessageDTO pDTO) throws Exception;

    int readTradeMessage(Long userId, TradeMessageDTO pDTO) throws Exception;

    List<TradeMessageRoomListDTO> getTradeMessageRoomList(Long userId) throws Exception;

    void validateTradeMessageRoom(Long userId, Long messageRoomId) throws Exception;
}