package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.GeminiReDiagDTO;
import kopo.poly.jolljack.dto.PapCandidateDTO;

import java.util.List;

public interface IPAPService {


    List<PapCandidateDTO> getTop3CandidatesProc(GeminiReDiagDTO pDTO) throws Exception;

}