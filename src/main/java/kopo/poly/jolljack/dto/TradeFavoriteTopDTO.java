package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record TradeFavoriteTopDTO(

        Long tradePostId,

        String title,

        Integer price,

        Long favoriteCount

) {
}