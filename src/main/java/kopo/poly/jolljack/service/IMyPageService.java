package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.TradePostDetailDTO;
import kopo.poly.jolljack.dto.TradePostListDTO;

import java.util.List;
import java.util.Map;

public interface IMyPageService {


     // 마이페이지 화면에 필요한 전체 데이터 조회
    Map<String, Object> getMyPageInfoProc(HttpSession session) throws Exception;

     // 선호 작물 수정
    Map<String, Object> updateFavoriteCropProc(HttpServletRequest request, HttpSession session) throws Exception;

    // 지역 수정
    Map<String, Object> updateRegionProc(HttpServletRequest request, HttpSession session) throws Exception;

    // 회원 탈퇴 처리
    Map<String, Object> withdrawUserProc(HttpSession session) throws Exception;

    // 관심 거래글 목록 조회
    List<TradePostListDTO> getFavoriteTradeListProc(Long userId) throws Exception;

    // 내가 등록한 거래글 조회
    List<TradePostListDTO> getMyTradeListProc(Long userId) throws Exception;

    // 내가 등록한 거래글 상태 변경
    Map<String, Object> updateMyTradeStatusProc(Long userId, TradePostDetailDTO pDTO) throws Exception;

    // 내가 등록한 거래글 삭제
    Map<String, Object> deleteMyTradePostProc(Long userId, TradePostDetailDTO pDTO) throws Exception;

}