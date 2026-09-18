package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.TradePostListDTO;
import kopo.poly.jolljack.dto.TradeSearchDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITradeMapper {

    Long getTradePostCount(TradeSearchDTO pDTO) throws Exception;

    List<TradePostListDTO> getTradePostList(TradeSearchDTO pDTO) throws Exception;

}