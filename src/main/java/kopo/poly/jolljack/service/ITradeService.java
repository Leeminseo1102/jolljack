package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.*;

import java.util.List;

public interface ITradeService {

    TradePageDTO getTradePostList(Long userId, TradeSearchDTO pDTO) throws Exception;

    List<RegionDTO> getSidoList() throws Exception;

    String getUserSidoName(Long userId) throws Exception;

    List<RegionDTO> getSigunguList(RegionDTO pDTO) throws Exception;

    String registerTradePost(Long userId, TradePostRegisterDTO pDTO) throws Exception;

    TradePostDetailDTO getTradePostDetail(Long userId, TradePostDetailDTO pDTO) throws Exception;

    TradePostDetailDTO toggleTradeFavorite(Long userId, TradeFavoriteDTO pDTO) throws Exception;

    List<TradeFavoriteTopDTO> getTradeFavoriteTop5() throws Exception;
}