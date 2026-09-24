package kopo.poly.jolljack.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record TradePostRegisterDTO(

        Long regionId,

        String title,

        String content,

        Integer price,

        Integer quantity,

        MultipartFile image
) {
}