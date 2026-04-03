package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.RegionDTO;

import java.util.List;
import java.util.Map;

public interface ISignupService {

    Map<String, Object> sendEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    Map<String, Object> verifyEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception;

    String checkStep1Session(HttpSession session) throws Exception;

    Map<String, Object> checkLoginIdProc(HttpServletRequest request) throws Exception;

    Map<String, Object> saveStep2Proc(HttpServletRequest request, HttpSession session) throws Exception;

    String checkStep2Session(HttpSession session) throws Exception;

    List<RegionDTO> getSidoList() throws Exception;

    List<RegionDTO> getSigunguListProc(HttpServletRequest request) throws Exception;

    List<CropDTO> getCropList() throws Exception;

    Map<String, Object> signupCompleteProc(HttpServletRequest request, HttpSession session) throws Exception;
}