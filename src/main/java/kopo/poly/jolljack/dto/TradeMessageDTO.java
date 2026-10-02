package kopo.poly.jolljack.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TradeMessageDTO {

    private Long messageId;

    private Long messageRoomId;

    private Long senderUserId;

    private String messageText;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}