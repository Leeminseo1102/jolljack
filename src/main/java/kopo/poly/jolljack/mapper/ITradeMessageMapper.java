package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.TradeMessageDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomDTO;
import kopo.poly.jolljack.dto.TradeMessageRoomListDTO;
import kopo.poly.jolljack.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITradeMessageMapper {

    TradeMessageRoomDTO getTradeMessageRoom(TradeMessageRoomDTO pDTO) throws Exception;

    int insertTradeMessageRoom(TradeMessageRoomDTO pDTO) throws Exception;

    TradeMessageRoomDTO getTradeMessageRoomById(TradeMessageRoomDTO pDTO) throws Exception;

    List<TradeMessageDTO> getTradeMessageList(TradeMessageDTO pDTO) throws Exception;

    int insertTradeMessage(TradeMessageDTO pDTO) throws Exception;

    int updateTradeMessageRoomLastMessage(TradeMessageRoomDTO pDTO) throws Exception;

    int updateTradeMessageRead(TradeMessageDTO pDTO) throws Exception;

    List<TradeMessageRoomListDTO> getTradeMessageRoomList(UserDTO pDTO) throws Exception;
}