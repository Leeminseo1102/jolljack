package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface ICropLoadingService {

    Map<String, Object> cropLoadingProc(HttpSession session) throws Exception;

}