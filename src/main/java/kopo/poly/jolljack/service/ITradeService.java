package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.TradePageDTO;
import kopo.poly.jolljack.dto.TradeSearchDTO;

public interface ITradeService {

    TradePageDTO getTradePostList(TradeSearchDTO pDTO) throws Exception;

}