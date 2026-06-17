package kopo.poly.jolljack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NonglimApiItemDTO {

    @JsonProperty("제목")
    private String title;

    @JsonProperty("요약글")
    private String summary;

    @JsonProperty("서브제목1")
    private String subTitle1;

    @JsonProperty("서브내용1")
    private String subContent1;

    @JsonProperty("서브제목2")
    private String subTitle2;

    @JsonProperty("서브내용2")
    private String subContent2;

    @JsonProperty("서브제목3")
    private String subTitle3;

    @JsonProperty("서브내용3")
    private String subContent3;

    @JsonProperty("서브제목4")
    private String subTitle4;

    @JsonProperty("서브내용4")
    private String subContent4;

    @JsonProperty("서브제목5")
    private String subTitle5;

    @JsonProperty("서브내용5")
    private String subContent5;
}