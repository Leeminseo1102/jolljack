package kopo.poly.jolljack.dto;

public record MyPageDTO(

        Long userId,

        String loginId,

        String name,

        String email,

        Long regionId,

        String fullRegionName,

        Long favoriteCropId,

        String favoriteCropName,

        String status,

        String createdAt,

        String updatedAt,

        String deletedAt
) {
}
