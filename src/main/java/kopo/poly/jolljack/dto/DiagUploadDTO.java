package kopo.poly.jolljack.dto;

import java.io.Serial;
import java.io.Serializable;

public record DiagUploadDTO(

        // S3에 저장된 이미지 MIME 타입
        String mimeType,

        // S3 객체 key
        String s3Key

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}