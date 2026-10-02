package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TradeMessageRoomDTO(

        Long messageRoomId,

        Long tradePostId,

        Long buyerUserId,

        String status,

        LocalDateTime createdAt,

        LocalDateTime lastMessageAt

) {
}