package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface IFindService {

    //아이디
    Map<String, Object> sendFindIdEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    Map<String, Object> verifyFindIdEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    Map<String, Object> findLoginIdProc(HttpSession session) throws Exception;


    //비번
    Map<String, Object> sendFindPwEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    Map<String, Object> verifyFindPwEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    String checkFindPwVerifiedSession(HttpSession session) throws Exception;

    Map<String, Object> resetPasswordProc(HttpServletRequest request, HttpSession session) throws Exception;

}