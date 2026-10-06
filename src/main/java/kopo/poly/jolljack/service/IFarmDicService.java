package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.FarmDicDetailDTO;
import kopo.poly.jolljack.dto.FarmDicSearchDTO;

import java.util.List;

public interface IFarmDicService {

    List<FarmDicSearchDTO> searchFrontMatch(String word, int pageNo) throws Exception;

    FarmDicDetailDTO detailWord(String wordNo) throws Exception;
}