package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TradePostDetailDTO(

        Long tradePostId,

        Long sellerUserId,

        String sellerName,

        Long regionId,

        String fullRegionName,

        String title,

        String content,

        Integer price,

        Integer quantity,

        String imageKey,

        String imageUrl,

        String status,

        Long favoriteCount,

        Boolean favorite,

        LocalDateTime createdAt

) {
}