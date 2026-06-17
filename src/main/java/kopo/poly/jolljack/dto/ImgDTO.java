package kopo.poly.jolljack.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImgDTO(

        // 사용자가 업로드한 병충해 이미지
        MultipartFile image

) {
}
