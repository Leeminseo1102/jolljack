package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.TradeMessageDTO;
import kopo.poly.jolljack.service.ISseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseService implements ISseService {

    private static final long SSE_TIMEOUT =
            60 * 60 * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap =
            new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long messageRoomId) throws Exception {

        log.info("{}.connect Start!", this.getClass().getName());

        SseEmitter emitter =
                new SseEmitter(SSE_TIMEOUT);

        emitterMap
                .computeIfAbsent(
                        messageRoomId,
                        key -> new CopyOnWriteArrayList<>()
                )
                .add(emitter);

        emitter.onCompletion(
                () -> removeEmitter(
                        messageRoomId,
                        emitter
                )
        );

        emitter.onTimeout(
                () -> removeEmitter(
                        messageRoomId,
                        emitter
                )
        );

        emitter.onError(
                throwable -> removeEmitter(
                        messageRoomId,
                        emitter
                )
        );

        try {

            emitter.send(
                    SseEmitter
                            .event()
                            .name("connect")
                            .data(messageRoomId)
            );

        } catch (IOException e) {

            removeEmitter(
                    messageRoomId,
                    emitter
            );

            emitter.completeWithError(e);

            throw e;
        }

        log.info("SSE 연결 messageRoomId : {}", messageRoomId);

        log.info("{}.connect End!", this.getClass().getName());

        return emitter;
    }

    @Override
    public void sendMessage(Long messageRoomId, TradeMessageDTO pDTO) {

        sendEvent(
                messageRoomId,
                "message",
                pDTO
        );
    }

    @Override
    public void sendRead(Long messageRoomId) {

        sendEvent(
                messageRoomId,
                "read",
                messageRoomId
        );
    }

    private void sendEvent(
            Long messageRoomId,
            String eventName,
            Object data
    ) {

        CopyOnWriteArrayList<SseEmitter> emitterList =
                emitterMap.get(messageRoomId);

        if (emitterList == null || emitterList.isEmpty()) {
            return;
        }

        emitterList.forEach(
                emitter -> {

                    try {

                        emitter.send(
                                SseEmitter
                                        .event()
                                        .name(eventName)
                                        .data(data)
                        );

                    } catch (Exception e) {

                        log.warn(
                                "SSE 전송 실패 messageRoomId : {}, eventName : {}",
                                messageRoomId,
                                eventName
                        );

                        removeEmitter(
                                messageRoomId,
                                emitter
                        );
                    }
                }
        );
    }

    private void removeEmitter(
            Long messageRoomId,
            SseEmitter emitter
    ) {

        CopyOnWriteArrayList<SseEmitter> emitterList =
                emitterMap.get(messageRoomId);

        if (emitterList == null) {
            return;
        }

        emitterList.remove(emitter);

        if (emitterList.isEmpty()) {

            emitterMap.remove(
                    messageRoomId,
                    emitterList
            );
        }
    }
}