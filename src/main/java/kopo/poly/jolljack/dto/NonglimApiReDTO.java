package kopo.poly.jolljack.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NonglimApiReDTO {

    private int currentCount;
    private int matchCount;
    private int page;
    private int perPage;
    private int totalCount;

    private List<NonglimApiItemDTO> data;
}