package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.dto.*;
import kopo.poly.jolljack.mapper.ITradeMapper;
import kopo.poly.jolljack.service.IImgService;
import kopo.poly.jolljack.service.IS3Service;
import kopo.poly.jolljack.service.ITradeService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService implements ITradeService {

    private final ITradeMapper tradeMapper;

    private final IImgService imgService;

    private final IS3Service s3Service;

    private static final int PAGE_SIZE = 10;


    @Override
    public TradePageDTO getTradePostList(Long userId, TradeSearchDTO pDTO) throws Exception {

        log.info("{}.getTradePostList Start!", this.getClass().getName());

        log.info("거래 목록 요청 userId : {}, sidoName : {}, page : {}",
                userId,
                pDTO.sidoName(),
                pDTO.page());

        String sidoName = pDTO.sidoName();

        if (sidoName == null || sidoName.isBlank()) {

            log.info("거래 지역 미지정 - 사용자 기본 지역 조회");

            sidoName = getUserSidoName(userId);

            log.info("사용자 기본 거래 지역 : {}", sidoName);

        } else {

            log.info("사용자가 선택한 거래 지역 사용 : {}", sidoName);
        }

        int page = pDTO.page() == null ? 1 : pDTO.page();
        int offset = (page - 1) * PAGE_SIZE;

        log.info("거래 페이징 page : {}, pageSize : {}, offset : {}",
                page,
                PAGE_SIZE,
                offset);

        TradeSearchDTO searchDTO = TradeSearchDTO.builder()
                .sidoName(sidoName)
                .page(page)
                .pageSize(PAGE_SIZE)
                .offset(offset)
                .build();

        Long totalCount = tradeMapper.getTradePostCount(searchDTO);

        log.info("거래 게시글 COUNT sidoName : {}, totalCount : {}",
                sidoName,
                totalCount);

        if (totalCount == 0) {

            log.info("거래 게시글 없음 sidoName : {}", sidoName);
            log.info("{}.getTradePostList End!", this.getClass().getName());

            return TradePageDTO.builder()
                    .totalCount(0L)
                    .page(page)
                    .pageSize(PAGE_SIZE)
                    .totalPages(0)
                    .tradeList(List.of())
                    .build();
        }

        List<TradePostListDTO> tradeList = tradeMapper.getTradePostList(searchDTO);

        log.info("거래 게시글 LIST 조회 완료 sidoName : {}, 조회건수 : {}",
                sidoName,
                tradeList.size());

        List<TradePostListDTO> resultList = new ArrayList<>();

        for (TradePostListDTO tradeDTO : tradeList) {

            String imageUrl = "";

            if (tradeDTO.imageKey() != null && !tradeDTO.imageKey().isBlank()) {
                imageUrl = s3Service.createTradePresignedUrl(tradeDTO.imageKey());
            }

            TradePostListDTO resultDTO = TradePostListDTO.builder()
                    .tradePostId(tradeDTO.tradePostId())
                    .regionId(tradeDTO.regionId())
                    .title(tradeDTO.title())
                    .price(tradeDTO.price())
                    .quantity(tradeDTO.quantity())
                    .imageKey(tradeDTO.imageKey())
                    .imageUrl(imageUrl)
                    .status(tradeDTO.status())
                    .createdAt(tradeDTO.createdAt())
                    .build();

            resultList.add(resultDTO);
        }

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        log.info("거래 페이지 계산 totalPages : {}", totalPages);
        log.info("{}.getTradePostList End!", this.getClass().getName());

        return TradePageDTO.builder()
                .totalCount(totalCount)
                .page(page)
                .pageSize(PAGE_SIZE)
                .totalPages(totalPages)
                .tradeList(resultList)
                .build();
    }

    @Override
    public List<RegionDTO> getSidoList() throws Exception {

        log.info("{}.getSidoList Start!",
                this.getClass().getName());


        List<RegionDTO> rList =
                tradeMapper.getSidoList();


        log.info("거래 시도 목록 조회 건수 : {}",
                rList.size());


        log.info("{}.getSidoList End!",
                this.getClass().getName());


        return rList;
    }


    @Override
    public String getUserSidoName(Long userId) throws Exception {

        log.info("{}.getUserSidoName Start!",
                this.getClass().getName());

        log.info("사용자 기본 시도 조회 userId : {}",
                userId);


        UserDTO userDTO = UserDTO.builder()
                .userId(userId)
                .build();


        Long regionId =
                tradeMapper.getRegionId(userDTO);


        log.info("사용자 regionId 조회 결과 : {}",
                regionId);


        if (regionId == null) {

            throw new IllegalStateException(
                    "사용자의 지역 정보를 찾을 수 없습니다."
            );
        }


        RegionDTO regionDTO = RegionDTO.builder()
                .regionId(regionId)
                .build();


        String sidoName =
                tradeMapper.getSidoName(regionDTO);


        log.info("regionId : {}, sidoName 조회 결과 : {}",
                regionId,
                sidoName);


        if (sidoName == null || sidoName.isBlank()) {

            throw new IllegalStateException(
                    "사용자의 시도 정보를 찾을 수 없습니다."
            );
        }


        log.info("{}.getUserSidoName End!",
                this.getClass().getName());


        return sidoName;
    }


    // 거래 등록
    @Override
    public List<RegionDTO> getSigunguList(RegionDTO pDTO) throws Exception {

        log.info("{}.getSigunguList Start!", this.getClass().getName());

        if (pDTO == null || CmmUtil.nvl(pDTO.getSidoName()).isEmpty()) {
            throw new IllegalArgumentException("시도 정보가 없습니다.");
        }

        List<RegionDTO> rList = tradeMapper.getSigunguList(pDTO);

        log.info("거래 등록 시군구 조회 sidoName : {}, 조회건수 : {}", pDTO.getSidoName(), rList.size());
        log.info("{}.getSigunguList End!", this.getClass().getName());

        return rList;
    }

    @Override
    public String registerTradePost(Long userId, TradePostRegisterDTO pDTO) throws Exception {

        log.info("{}.registerTradePost Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        if (pDTO == null) {
            throw new IllegalArgumentException("거래글 정보가 없습니다.");
        }

        String title = CmmUtil.nvl(pDTO.title()).trim();
        String content = CmmUtil.nvl(pDTO.content()).trim();

        if (pDTO.regionId() == null) {
            throw new IllegalArgumentException("거래 지역 정보가 없습니다.");
        }

        if (title.isEmpty()) {
            throw new IllegalArgumentException("상품 제목 정보가 없습니다.");
        }

        if (content.isEmpty()) {
            throw new IllegalArgumentException("상품 설명 정보가 없습니다.");
        }

        if (pDTO.price() == null || pDTO.price() < 0) {
            throw new IllegalArgumentException("상품 가격 정보가 올바르지 않습니다.");
        }

        if (pDTO.quantity() == null || pDTO.quantity() < 1) {
            throw new IllegalArgumentException("상품 수량 정보가 올바르지 않습니다.");
        }

        RegionDTO regionDTO = RegionDTO.builder()
                .regionId(pDTO.regionId())
                .build();

        String sidoName = tradeMapper.getSidoName(regionDTO);

        if (CmmUtil.nvl(sidoName).isEmpty()) {
            throw new IllegalArgumentException("선택한 거래 지역을 찾을 수 없습니다.");
        }

        log.info("거래글 등록 지역 확인 regionId : {}, sidoName : {}", pDTO.regionId(), sidoName);

        ImgDTO imgDTO = new ImgDTO(pDTO.image());

        imgService.validateImage(imgDTO);

        String imageKey = null;

        try {

            imageKey = s3Service.uploadTradeImage(pDTO.image());

            log.info("거래 대표 이미지 업로드 완료 : {}", imageKey);

            TradeInsertDTO insertDTO = TradeInsertDTO.builder()
                    .sellerUserId(userId)
                    .regionId(pDTO.regionId())
                    .title(title)
                    .content(content)
                    .price(pDTO.price())
                    .quantity(pDTO.quantity())
                    .imageKey(imageKey)
                    .status("SALE")
                    .build();

            int res = tradeMapper.insertTradePost(insertDTO);

            log.info("거래글 등록 DB 결과 : {}", res);

            if (res != 1) {
                throw new Exception("거래글 등록에 실패했습니다.");
            }

        } catch (Exception e) {

            if (imageKey != null && !imageKey.isBlank()) {

                try {

                    s3Service.deleteTradeImage(imageKey);

                    log.info("거래글 등록 실패로 S3 이미지 삭제 완료 : {}", imageKey);

                } catch (Exception deleteException) {

                    log.error("거래글 등록 실패 후 S3 이미지 삭제 실패 : {}", imageKey, deleteException);
                }
            }

            throw e;
        }

        log.info("거래글 등록 완료 userId : {}, regionId : {}, sidoName : {}", userId, pDTO.regionId(), sidoName);
        log.info("{}.registerTradePost End!", this.getClass().getName());

        return sidoName;
    }

    //거래글 상세글 및 좋아요

    @Override
    public TradePostDetailDTO getTradePostDetail(Long userId, TradePostDetailDTO pDTO) throws Exception {

        log.info("{}.getTradePostDetail Start!", this.getClass().getName());

        log.info("거래 상세 조회 userId : {}, tradePostId : {}",
                userId,
                pDTO.tradePostId());

        TradePostDetailDTO detailDTO =
                tradeMapper.getTradePostDetail(pDTO);

        if (detailDTO == null) {

            log.info("거래 상세 게시글 없음 tradePostId : {}",
                    pDTO.tradePostId());

            log.info("{}.getTradePostDetail End!", this.getClass().getName());

            return null;
        }

        Long favoriteCount =
                tradeMapper.getTradeFavoriteCount(pDTO);

        TradeFavoriteDTO favoriteDTO = TradeFavoriteDTO.builder()
                .tradePostId(pDTO.tradePostId())
                .userId(userId)
                .build();

        Long favoriteCheck =
                tradeMapper.getTradeFavoriteCheck(favoriteDTO);

        boolean favorite =
                favoriteCheck != null && favoriteCheck > 0;

        String imageUrl = "";

        if (detailDTO.imageKey() != null && !detailDTO.imageKey().isBlank()) {

            imageUrl =
                    s3Service.createTradePresignedUrl(detailDTO.imageKey());
        }

        TradePostDetailDTO resultDTO = TradePostDetailDTO.builder()
                .tradePostId(detailDTO.tradePostId())
                .sellerUserId(detailDTO.sellerUserId())
                .sellerName(detailDTO.sellerName())
                .regionId(detailDTO.regionId())
                .fullRegionName(detailDTO.fullRegionName())
                .title(detailDTO.title())
                .content(detailDTO.content())
                .price(detailDTO.price())
                .quantity(detailDTO.quantity())
                .imageKey(detailDTO.imageKey())
                .imageUrl(imageUrl)
                .status(detailDTO.status())
                .favoriteCount(favoriteCount)
                .favorite(favorite)
                .createdAt(detailDTO.createdAt())
                .build();

        log.info("거래 상세 조회 완료 tradePostId : {}, favoriteCount : {}, favorite : {}",
                resultDTO.tradePostId(),
                resultDTO.favoriteCount(),
                resultDTO.favorite());

        log.info("{}.getTradePostDetail End!", this.getClass().getName());

        return resultDTO;
    }

}