package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.TradePageDTO;
import kopo.poly.jolljack.dto.TradePostListDTO;
import kopo.poly.jolljack.dto.TradeSearchDTO;
import kopo.poly.jolljack.mapper.ITradeMapper;
import kopo.poly.jolljack.service.ITradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService implements ITradeService {

    private final ITradeMapper tradeMapper;

    private static final int PAGE_SIZE = 10;


    @Override
    public TradePageDTO getTradePostList(TradeSearchDTO pDTO) throws Exception {

        log.info("{}.getTradePostList Start!",
                this.getClass().getName());


        Long totalCount = tradeMapper.getTradePostCount(pDTO);


        if (totalCount == 0) {

            log.info("거래 게시글 없음");

            return TradePageDTO.builder()
                    .totalCount(0L)
                    .page(pDTO.page())
                    .pageSize(PAGE_SIZE)
                    .totalPages(0)
                    .tradeList(List.of())
                    .build();
        }


        int offset = (pDTO.page() - 1) * PAGE_SIZE;


        TradeSearchDTO searchDTO = TradeSearchDTO.builder()
                .regionId(pDTO.regionId())
                .page(pDTO.page())
                .pageSize(PAGE_SIZE)
                .offset(offset)
                .build();


        List<TradePostListDTO> tradeList =
                tradeMapper.getTradePostList(searchDTO);


        int totalPages =
                (int) Math.ceil((double) totalCount / PAGE_SIZE);


        log.info("{}.getTradePostList End!",
                this.getClass().getName());


        return TradePageDTO.builder()
                .totalCount(totalCount)
                .page(pDTO.page())
                .pageSize(PAGE_SIZE)
                .totalPages(totalPages)
                .tradeList(tradeList)
                .build();
    }
}