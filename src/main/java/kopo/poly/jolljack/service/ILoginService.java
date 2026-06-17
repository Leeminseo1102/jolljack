package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface ILoginService {

    // 로그인 처리 (유효성 검증 + DB 조회 + 세션 저장)
    Map<String, Object> loginProc(HttpServletRequest request, HttpSession session) throws Exception;

    // 로그아웃 처리 (세션 초기화)
    void logoutProc(HttpSession session) throws Exception;

    /**
     * 탈퇴 계정 복구 처리
     */
    Map<String, Object> restoreUserProc(HttpSession session) throws Exception;

}
