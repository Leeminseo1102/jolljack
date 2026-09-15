package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TradePageDTO(
        Long totalCount,

        Integer page,

        Integer pageSize,

        Integer totalPages,

        List<TradePostListDTO> tradeList
) {
}