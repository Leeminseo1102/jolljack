package kopo.poly.jolljack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiReDTO(
        List<CandidateDTO> candidates
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CandidateDTO(
            ContentDTO content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDTO(
            List<PartDTO> parts
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PartDTO(
            String text,
            String thoughtSignature
    ) {
    }
}