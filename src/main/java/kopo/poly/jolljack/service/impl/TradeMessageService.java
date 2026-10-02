package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.*;
import kopo.poly.jolljack.mapper.ITradeMapper;
import kopo.poly.jolljack.mapper.ITradeMessageMapper;
import kopo.poly.jolljack.service.ITradeMessageService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeMessageService implements ITradeMessageService {

    private final ITradeMessageMapper tradeMessageMapper;

    private final ITradeMapper tradeMapper;


    @Override
    @Transactional
    public TradeMessageRoomDTO getOrCreateTradeMessageRoom(Long userId, TradeMessageRoomDTO pDTO) throws Exception {

        log.info("{}.getOrCreateTradeMessageRoom Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        if (pDTO == null || pDTO.tradePostId() == null) {
            throw new IllegalArgumentException("거래글 정보가 없습니다.");
        }

        TradePostDetailDTO tradeDTO = TradePostDetailDTO.builder()
                .tradePostId(pDTO.tradePostId())
                .build();

        TradePostDetailDTO tradePostDTO =
                tradeMapper.getTradePostDetail(tradeDTO);

        if (tradePostDTO == null) {
            throw new IllegalArgumentException("거래글을 찾을 수 없습니다.");
        }

        if (userId.equals(tradePostDTO.sellerUserId())) {
            throw new IllegalArgumentException("본인의 거래글에는 채팅을 시작할 수 없습니다.");
        }

        TradeMessageRoomDTO roomDTO = TradeMessageRoomDTO.builder()
                .tradePostId(pDTO.tradePostId())
                .buyerUserId(userId)
                .build();

        TradeMessageRoomDTO rDTO =
                tradeMessageMapper.getTradeMessageRoom(roomDTO);

        if (rDTO != null) {

            log.info("기존 거래 채팅방 조회 messageRoomId : {}",
                    rDTO.messageRoomId());

            log.info("{}.getOrCreateTradeMessageRoom End!", this.getClass().getName());

            return rDTO;
        }

        int res =
                tradeMessageMapper.insertTradeMessageRoom(roomDTO);

        if (res != 1) {
            throw new Exception("거래 채팅방 생성에 실패했습니다.");
        }

        rDTO =
                tradeMessageMapper.getTradeMessageRoom(roomDTO);

        if (rDTO == null) {
            throw new Exception("생성된 거래 채팅방을 찾을 수 없습니다.");
        }

        log.info("거래 채팅방 생성 완료 messageRoomId : {}",
                rDTO.messageRoomId());

        log.info("{}.getOrCreateTradeMessageRoom End!", this.getClass().getName());

        return rDTO;
    }


    @Override
    public List<TradeMessageDTO> getTradeMessageList(Long userId, TradeMessageDTO pDTO) throws Exception {

        log.info("{}.getTradeMessageList Start!", this.getClass().getName());

        if (pDTO == null || pDTO.getMessageRoomId() == null) {
            throw new IllegalArgumentException("채팅방 정보가 없습니다.");
        }

        validateTradeMessageRoom(userId, pDTO.getMessageRoomId());

        List<TradeMessageDTO> rList =
                tradeMessageMapper.getTradeMessageList(pDTO);

        log.info("거래 메시지 조회 messageRoomId : {}, 조회건수 : {}",
                pDTO.getMessageRoomId(),
                rList.size());

        log.info("{}.getTradeMessageList End!", this.getClass().getName());

        return rList;
    }


    @Override
    @Transactional
    public TradeMessageDTO sendTradeMessage(Long userId, TradeMessageDTO pDTO) throws Exception {

        log.info("{}.sendTradeMessage Start!", this.getClass().getName());

        if (pDTO == null || pDTO.getMessageRoomId() == null) {
            throw new IllegalArgumentException("채팅방 정보가 없습니다.");
        }

        String messageText =
                CmmUtil.nvl(pDTO.getMessageText()).trim();

        if (messageText.isEmpty()) {
            throw new IllegalArgumentException("메시지 내용을 입력해주세요.");
        }

        validateTradeMessageRoom(
                userId,
                pDTO.getMessageRoomId()
        );

        TradeMessageDTO insertDTO = TradeMessageDTO.builder()
                .messageRoomId(pDTO.getMessageRoomId())
                .senderUserId(userId)
                .messageText(messageText)
                .build();

        int res =
                tradeMessageMapper.insertTradeMessage(insertDTO);

        if (res != 1) {
            throw new Exception("메시지 전송에 실패했습니다.");
        }

        TradeMessageRoomDTO roomDTO = TradeMessageRoomDTO.builder()
                .messageRoomId(pDTO.getMessageRoomId())
                .build();

        tradeMessageMapper.updateTradeMessageRoomLastMessage(roomDTO);

        log.info("거래 메시지 등록 완료 messageId : {}, messageRoomId : {}, senderUserId : {}",
                insertDTO.getMessageId(),
                insertDTO.getMessageRoomId(),
                userId);

        log.info("{}.sendTradeMessage End!", this.getClass().getName());

        return insertDTO;
    }


    @Override
    @Transactional
    public int readTradeMessage(Long userId, TradeMessageDTO pDTO) throws Exception {

        log.info("{}.readTradeMessage Start!", this.getClass().getName());

        if (pDTO == null || pDTO.getMessageRoomId() == null) {
            throw new IllegalArgumentException("채팅방 정보가 없습니다.");
        }

        validateTradeMessageRoom(
                userId,
                pDTO.getMessageRoomId()
        );

        TradeMessageDTO readDTO = TradeMessageDTO.builder()
                .messageRoomId(pDTO.getMessageRoomId())
                .senderUserId(userId)
                .build();

        int res =
                tradeMessageMapper.updateTradeMessageRead(readDTO);

        log.info("거래 메시지 읽음 처리 messageRoomId : {}, 처리건수 : {}",
                pDTO.getMessageRoomId(),
                res);

        log.info("{}.readTradeMessage End!", this.getClass().getName());

        return res;
    }

    @Override
    public void validateTradeMessageRoom(Long userId, Long messageRoomId) throws Exception {

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        TradeMessageRoomDTO roomDTO = TradeMessageRoomDTO.builder()
                .messageRoomId(messageRoomId)
                .build();

        TradeMessageRoomDTO room =
                tradeMessageMapper.getTradeMessageRoomById(roomDTO);

        if (room == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다.");
        }

        TradePostDetailDTO tradeDTO = TradePostDetailDTO.builder()
                .tradePostId(room.tradePostId())
                .build();

        TradePostDetailDTO tradePostDTO =
                tradeMapper.getTradePostDetail(tradeDTO);

        if (tradePostDTO == null) {
            throw new IllegalArgumentException("거래글을 찾을 수 없습니다.");
        }

        boolean buyer =
                userId.equals(room.buyerUserId());

        boolean seller =
                userId.equals(tradePostDTO.sellerUserId());

        if (!buyer && !seller) {
            throw new IllegalArgumentException("채팅방에 접근할 권한이 없습니다.");
        }
    }

    @Override
    public List<TradeMessageRoomListDTO> getTradeMessageRoomList(Long userId) throws Exception {

        log.info("{}.getTradeMessageRoomList Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        UserDTO pDTO = new UserDTO();

        pDTO.setUserId(userId);

        List<TradeMessageRoomListDTO> rList =
                tradeMessageMapper.getTradeMessageRoomList(pDTO);

        log.info("거래 채팅방 목록 조회건수 : {}", rList.size());

        log.info("{}.getTradeMessageRoomList End!", this.getClass().getName());

        return rList;
    }
}