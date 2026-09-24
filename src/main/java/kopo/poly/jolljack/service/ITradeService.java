package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.TradePageDTO;
import kopo.poly.jolljack.dto.TradePostRegisterDTO;
import kopo.poly.jolljack.dto.TradeSearchDTO;
import kopo.poly.jolljack.dto.TradePostDetailDTO;

import java.util.List;

public interface ITradeService {

    TradePageDTO getTradePostList(Long userId, TradeSearchDTO pDTO) throws Exception;

    List<RegionDTO> getSidoList() throws Exception;

    String getUserSidoName(Long userId) throws Exception;

    List<RegionDTO> getSigunguList(RegionDTO pDTO) throws Exception;

    String registerTradePost(Long userId, TradePostRegisterDTO pDTO) throws Exception;

    TradePostDetailDTO getTradePostDetail(Long userId, TradePostDetailDTO pDTO) throws Exception;
}