package kopo.poly.jolljack.service;

import kopo.poly.jolljack.dto.DiagUploadDTO;
import kopo.poly.jolljack.dto.ImgDTO;
import kopo.poly.jolljack.dto.ImgGeminiDTO;

public interface IImgService {

    void validateImage(ImgDTO pDTO) throws Exception;

    ImgGeminiDTO makeImgGeminiDTO(DiagUploadDTO pDTO) throws Exception;

    DiagUploadDTO uploadDiagImageProc(ImgDTO pDTO) throws Exception;

}
