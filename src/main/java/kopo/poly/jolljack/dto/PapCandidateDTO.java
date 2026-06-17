package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PapCandidateDTO(

        String diseaseName,


        String feature,


        String occurrenceContent,


        String controlContent,


        int score,


        List<String> matchedKeywords
) {
}