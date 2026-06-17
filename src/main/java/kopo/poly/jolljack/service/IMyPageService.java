package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

}