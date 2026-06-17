package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.DiagUploadDTO;
import kopo.poly.jolljack.dto.ImgDTO;
import kopo.poly.jolljack.dto.ImgGeminiDTO;
import kopo.poly.jolljack.service.IImgService;
import kopo.poly.jolljack.service.IS3Service;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImgService implements IImgService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final IS3Service s3Service;

    @Override
    public void validateImage(ImgDTO pDTO) throws Exception {

        if (pDTO == null) {
            throw new IllegalArgumentException("이미지 요청 정보가 없습니다.");
        }

        MultipartFile image = pDTO.image();

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("업로드된 이미지 파일이 없습니다.");
        }

        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("이미지 파일은 최대 5MB까지 업로드할 수 있습니다.");
        }

        String mimeType = CmmUtil.nvl(image.getContentType());

        if (!isAllowedImageMimeType(mimeType)) {
            throw new IllegalArgumentException("jpg, png, webp 형식의 이미지만 업로드할 수 있습니다.");
        }
    }

    @Override
    public DiagUploadDTO uploadDiagImageProc(ImgDTO pDTO) throws Exception {

        log.info("{}.uploadDiagImageProc Start!", this.getClass().getName());

        validateImage(pDTO);

        MultipartFile image = pDTO.image();

        String mimeType = CmmUtil.nvl(image.getContentType());

        String s3Key = s3Service.uploadImage(image);

        DiagUploadDTO rDTO = new DiagUploadDTO(mimeType, s3Key);

        log.info("image mimeType : {}", rDTO.mimeType());
        log.info("S3 image uploaded. s3Key created.");
        log.info("{}.uploadDiagImageProc End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public ImgGeminiDTO makeImgGeminiDTO(DiagUploadDTO pDTO) throws Exception {

        log.info("{}.makeImgGeminiDTO Start!", this.getClass().getName());

        if (pDTO == null) {
            throw new IllegalArgumentException("업로드된 이미지 정보가 없습니다.");
        }

        String mimeType = CmmUtil.nvl(pDTO.mimeType());
        String s3Key = CmmUtil.nvl(pDTO.s3Key());

        if (mimeType.isEmpty()) {
            throw new IllegalArgumentException("이미지 MIME 타입이 없습니다.");
        }

        if (s3Key.isEmpty()) {
            throw new IllegalArgumentException("S3 이미지 키가 없습니다.");
        }

        String fileUri = s3Service.createPresignedUrl(s3Key);

        if (CmmUtil.nvl(fileUri).isEmpty()) {
            throw new Exception("Gemini 요청용 이미지 URL 생성에 실패했습니다.");
        }

        ImgGeminiDTO rDTO = new ImgGeminiDTO(mimeType, fileUri);

        log.info("Gemini image DTO created. mimeType : {}", rDTO.mimeType());
        log.info("{}.makeImgGeminiDTO End!", this.getClass().getName());

        return rDTO;
    }

    private boolean isAllowedImageMimeType(String mimeType) {

        mimeType = CmmUtil.nvl(mimeType);

        return mimeType.equals("image/jpeg")
                || mimeType.equals("image/png")
                || mimeType.equals("image/webp");
    }



}