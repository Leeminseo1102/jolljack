package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.NonglimDTO;

import java.util.List;

public interface INonglimService {


    List<NonglimDTO> getNonglimList() throws Exception;


    String getNonglimPrompt() throws Exception;
}