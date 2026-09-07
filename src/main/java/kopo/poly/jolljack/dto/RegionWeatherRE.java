package kopo.poly.jolljack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegionWeatherRE(
        Response response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Body body
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            Items items
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            List<Item> item
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String category,
            String fcstDate,
            String fcstTime,
            String fcstValue
    ) {}
}