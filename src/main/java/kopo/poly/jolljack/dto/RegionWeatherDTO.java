package kopo.poly.jolljack.dto;

public record RegionWeatherDTO(
        String region,
        String temp,
        String weather,
        String humidity,
        String rainfall,
        String rainChance
) {
}