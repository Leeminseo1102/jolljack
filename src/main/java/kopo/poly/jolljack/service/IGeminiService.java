package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.*;

import java.util.List;

public interface IGeminiService {

    GeminiCropReDTO getCropAnalysis(WeatherSumDTO pDTO, String nonglimPrompt) throws Exception;


    GeminiReDiagDTO getDiagFeature(ImgGeminiDTO pDTO) throws Exception;

    DiDiDTO getDiagFinalResultProc(GeminiReDiagDTO featureDTO, List<PapCandidateDTO> top3List) throws Exception;


}