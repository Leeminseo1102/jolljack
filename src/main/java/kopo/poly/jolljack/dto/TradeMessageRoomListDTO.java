package kopo.poly.jolljack.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TradeMessageRoomListDTO(

        Long messageRoomId,

        Long tradePostId,

        String tradePostTitle,

        Long opponentUserId,

        String opponentName,

        String lastMessageText,

        LocalDateTime lastMessageAt,

        Long unreadCount

) {
}