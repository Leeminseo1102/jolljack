package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TradePostListDTO(

        Long tradePostId,

        Long regionId,

        String title,

        Integer price,

        Integer quantity,

        String imageKey,

        String status,

        LocalDateTime createdAt

) {
}
