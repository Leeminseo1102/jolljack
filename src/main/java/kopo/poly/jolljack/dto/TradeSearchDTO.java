package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record TradeSearchDTO(

        String sidoName,

        Integer page,

        Integer pageSize,

        Integer offset

) {
}