package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.TradePageDTO;
import kopo.poly.jolljack.dto.TradePostListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITradeMapper {

    Long getTradePostCount(TradePageDTO pDTO) throws Exception;

    List<TradePostListDTO> getTradePostList(TradePageDTO pDTO) throws Exception;
}