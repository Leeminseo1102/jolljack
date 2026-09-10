package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record RegionGridDTO(

        Long userId,

        Long regionId,

        String region,

        Integer nx,

        Integer ny
) {
}