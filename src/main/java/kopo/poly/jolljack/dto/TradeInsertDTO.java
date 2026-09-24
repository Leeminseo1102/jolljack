package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TradeInsertDTO(

        Long tradePostId,

        Long sellerUserId,

        Long regionId,

        String title,

        String content,

        Integer price,

        Integer quantity,

        String imageKey,

        String status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}