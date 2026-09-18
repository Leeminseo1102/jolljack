package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record TradeSearchDTO(

        Long regionId,

        Integer page,

        Integer pageSize,

        Integer offset

) {
}