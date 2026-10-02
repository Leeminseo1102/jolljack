package kopo.poly.jolljack.mapper;

import kopo.poly.jolljack.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITradeMapper {

    Long getRegionId(UserDTO pDTO) throws Exception;

    String getSidoName(RegionDTO pDTO) throws Exception;

    List<RegionDTO> getSidoList() throws Exception;

    Long getTradePostCount(TradeSearchDTO pDTO) throws Exception;

    List<TradePostListDTO> getTradePostList(TradeSearchDTO pDTO) throws Exception;

    List<RegionDTO> getSigunguList(RegionDTO pDTO) throws Exception;

    int insertTradePost(TradeInsertDTO pDTO) throws Exception;

    TradePostDetailDTO getTradePostDetail(TradePostDetailDTO pDTO) throws Exception;

    Long getTradeFavoriteCount(TradePostDetailDTO pDTO) throws Exception;

    Long getTradeFavoriteCheck(TradeFavoriteDTO pDTO) throws Exception;

    int insertTradeFavorite(TradeFavoriteDTO pDTO) throws Exception;

    int deleteTradeFavorite(TradeFavoriteDTO pDTO) throws Exception;

    List<TradeFavoriteTopDTO> getTradeFavoriteTop5() throws Exception;
}