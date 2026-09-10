package kopo.poly.jolljack.dto;

import lombok.Builder;

@Builder
public record RegionWeatherDTO(
        String region,
        String temp,
        String weather,
        String humidity,
        String rainfall,
        String rainChance
) {
}