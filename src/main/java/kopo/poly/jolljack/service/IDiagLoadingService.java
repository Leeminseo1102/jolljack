package kopo.poly.jolljack.service;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface IDiagLoadingService {

    Map<String, Object> diagLoadingProc(HttpSession session) throws Exception;

}