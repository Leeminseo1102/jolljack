package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record TradeFavoriteDTO(

        Long tradePostId,

        Long userId

) {
}