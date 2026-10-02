package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropAnalysisDTO;
import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.DiseaseDiagnosisDTO;
import kopo.poly.jolljack.dto.MyPageDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.UserDTO;
import kopo.poly.jolljack.dto.TradePostListDTO;
import kopo.poly.jolljack.dto.TradePostDetailDTO;
import kopo.poly.jolljack.mapper.IMyPageMapper;
import kopo.poly.jolljack.mapper.ISignupMapper;
import kopo.poly.jolljack.service.IMyPageService;
import kopo.poly.jolljack.service.IS3Service;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageService implements IMyPageService {

    private final IMyPageMapper myPageMapper;

    // 기존 회원가입 Mapper 재사용
    // crop 목록 조회, 시도+시군구로 regionId 조회에 사용
    private final ISignupMapper signupMapper;

    private final IS3Service s3Service;

    /**
     * 마이페이지 화면 데이터 조회
     */
    @Override
    public Map<String, Object> getMyPageInfoProc(HttpSession session) throws Exception {

        log.info("{}.getMyPageInfoProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Object sessionUserId = session.getAttribute("userId");

        if (sessionUserId == null) {
            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");
            return rMap;
        }

        Long userId = Long.valueOf(String.valueOf(sessionUserId));

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);

        MyPageDTO userInfo = myPageMapper.getUserInfo(pDTO);

        if (userInfo == null) {
            rMap.put("result", "fail");
            rMap.put("msg", "회원 정보를 찾을 수 없습니다.");
            return rMap;
        }

        List<CropDTO> cropList = signupMapper.getCropList();
        List<CropAnalysisDTO> analysisList = myPageMapper.getAnalysisList(pDTO);
        List<DiseaseDiagnosisDTO> diagnosisList = myPageMapper.getDiagnosisList(pDTO);

        rMap.put("result", "success");
        rMap.put("msg", "조회 성공");
        rMap.put("userInfo", userInfo);
        rMap.put("cropList", cropList);
        rMap.put("analysisList", analysisList);
        rMap.put("diagnosisList", diagnosisList);

        log.info("{}.getMyPageInfoProc End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 선호 작물 수정
     */
    @Override
    public Map<String, Object> updateFavoriteCropProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateFavoriteCropProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Object sessionUserId = session.getAttribute("userId");

        if (sessionUserId == null) {
            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");
            return rMap;
        }

        String favoriteCropId = CmmUtil.nvl(request.getParameter("favoriteCropId"));

        if (favoriteCropId.isEmpty()) {
            rMap.put("result", "fail");
            rMap.put("msg", "선택된 작물이 없습니다.");
            return rMap;
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(Long.valueOf(String.valueOf(sessionUserId)));
        pDTO.setFavoriteCropId(Long.valueOf(favoriteCropId));

        int res = myPageMapper.updateFavoriteCrop(pDTO);

        if (res > 0) {
            rMap.put("result", "success");
            rMap.put("msg", "선호 작물이 수정되었습니다.");
        } else {
            rMap.put("result", "fail");
            rMap.put("msg", "선호 작물 수정에 실패했습니다.");
        }

        log.info("{}.updateFavoriteCropProc End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 지역 수정
     *
     * 방식 1:
     * - request에 regionId가 직접 넘어오면 바로 업데이트
     *
     * 방식 2:
     * - sidoName, sigunguName이 넘어오면 기존 회원가입 Mapper의 getRegionId() 재사용
     */
    @Override
    public Map<String, Object> updateRegionProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateRegionProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Object sessionUserId = session.getAttribute("userId");

        if (sessionUserId == null) {
            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");
            return rMap;
        }

        String regionId = CmmUtil.nvl(request.getParameter("regionId"));
        String sidoName = CmmUtil.nvl(request.getParameter("sidoName"));
        String sigunguName = CmmUtil.nvl(request.getParameter("sigunguName"));

        Long finalRegionId = null;

        if (!regionId.isEmpty()) {
            finalRegionId = Long.valueOf(regionId);

        } else if (!sidoName.isEmpty() && !sigunguName.isEmpty()) {

            RegionDTO pRegionDTO = new RegionDTO();
            pRegionDTO.setSidoName(sidoName);
            pRegionDTO.setSigunguName(sigunguName);

            RegionDTO rRegionDTO = signupMapper.getRegionId(pRegionDTO);

            if (rRegionDTO != null) {
                finalRegionId = rRegionDTO.getRegionId();
            }
        }

        if (finalRegionId == null) {
            rMap.put("result", "fail");
            rMap.put("msg", "지역 정보를 찾을 수 없습니다.");
            return rMap;
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(Long.valueOf(String.valueOf(sessionUserId)));
        pDTO.setRegionId(finalRegionId);

        int res = myPageMapper.updateRegion(pDTO);

        if (res > 0) {
            rMap.put("result", "success");
            rMap.put("msg", "지역이 수정되었습니다.");
        } else {
            rMap.put("result", "fail");
            rMap.put("msg", "지역 수정에 실패했습니다.");
        }

        log.info("{}.updateRegionProc End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 회원 탈퇴
     * - 실제 삭제 X
     * - users.status = DELETED
     * - users.deleted_at = NOW()
     */
    @Override
    public Map<String, Object> withdrawUserProc(HttpSession session) throws Exception {

        log.info("{}.withdrawUserProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        Object sessionUserId = session.getAttribute("userId");

        if (sessionUserId == null) {
            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");
            return rMap;
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(Long.valueOf(String.valueOf(sessionUserId)));

        int res = myPageMapper.withdrawUser(pDTO);

        if (res > 0) {
            session.invalidate();

            rMap.put("result", "success");
            rMap.put("msg", "회원 탈퇴가 처리되었습니다.");
        } else {
            rMap.put("result", "fail");
            rMap.put("msg", "회원 탈퇴 처리에 실패했습니다.");
        }

        log.info("{}.withdrawUserProc End!", this.getClass().getName());

        return rMap;
    }

    /**
     * 관심 거래글 목록 조회
     */
    @Override
    public List<TradePostListDTO> getFavoriteTradeListProc(Long userId) throws Exception {

        log.info("{}.getFavoriteTradeListProc Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);

        List<TradePostListDTO> favoriteList =
                myPageMapper.getFavoriteTradeList(pDTO);

        for (int i = 0; i < favoriteList.size(); i++) {

            TradePostListDTO tradeDTO =
                    favoriteList.get(i);

            String imageUrl = "";

            if (tradeDTO.imageKey() != null && !tradeDTO.imageKey().isBlank()) {

                imageUrl =
                        s3Service.createTradePresignedUrl(tradeDTO.imageKey());
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
                    .favorite(tradeDTO.favorite())
                    .createdAt(tradeDTO.createdAt())
                    .build();

            favoriteList.set(i, resultDTO);
        }

        log.info("관심 거래글 조회 완료 userId : {}, 조회건수 : {}",
                userId,
                favoriteList.size());

        log.info("{}.getFavoriteTradeListProc End!", this.getClass().getName());

        return favoriteList;
    }

    /**
     * 내가 등록한 거래글 조회
     */
    @Override
    public List<TradePostListDTO> getMyTradeListProc(Long userId) throws Exception {

        log.info("{}.getMyTradeListProc Start!", this.getClass().getName());

        if (userId == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);

        List<TradePostListDTO> myTradeList =
                myPageMapper.getMyTradeList(pDTO);

        for (int i = 0; i < myTradeList.size(); i++) {

            TradePostListDTO tradeDTO =
                    myTradeList.get(i);

            String imageUrl = "";

            if (tradeDTO.imageKey() != null && !tradeDTO.imageKey().isBlank()) {

                imageUrl =
                        s3Service.createTradePresignedUrl(tradeDTO.imageKey());
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
                    .favorite(tradeDTO.favorite())
                    .createdAt(tradeDTO.createdAt())
                    .build();

            myTradeList.set(i, resultDTO);
        }

        log.info("내가 등록한 거래글 조회 완료 userId : {}, 조회건수 : {}",
                userId,
                myTradeList.size());

        log.info("{}.getMyTradeListProc End!", this.getClass().getName());

        return myTradeList;
    }


    /**
     * 내가 등록한 거래글 상태 변경
     */
    @Override
    public Map<String, Object> updateMyTradeStatusProc(Long userId, TradePostDetailDTO pDTO) throws Exception {

        log.info("{}.updateMyTradeStatusProc Start!", this.getClass().getName());

        Map<String, Object> rMap =
                new HashMap<>();

        if (userId == null) {

            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");

            return rMap;
        }

        if (pDTO == null || pDTO.tradePostId() == null) {

            rMap.put("result", "fail");
            rMap.put("msg", "거래글 정보가 없습니다.");

            return rMap;
        }

        String status =
                pDTO.status();

        if (!"SALE".equals(status)
                && !"RESERVED".equals(status)
                && !"SOLD".equals(status)) {

            rMap.put("result", "fail");
            rMap.put("msg", "올바르지 않은 거래 상태입니다.");

            return rMap;
        }

        TradePostDetailDTO updateDTO = TradePostDetailDTO.builder()
                .tradePostId(pDTO.tradePostId())
                .sellerUserId(userId)
                .status(status)
                .build();

        int res =
                myPageMapper.updateMyTradeStatus(updateDTO);

        if (res > 0) {

            rMap.put("result", "success");
            rMap.put("msg", "거래 상태가 변경되었습니다.");
            rMap.put("status", status);

        } else {

            rMap.put("result", "fail");
            rMap.put("msg", "거래 상태 변경에 실패했습니다.");
        }

        log.info("거래 상태 변경 userId : {}, tradePostId : {}, status : {}, result : {}",
                userId,
                pDTO.tradePostId(),
                status,
                res);

        log.info("{}.updateMyTradeStatusProc End!", this.getClass().getName());

        return rMap;
    }


    /**
     * 내가 등록한 거래글 삭제
     */
    @Override
    public Map<String, Object> deleteMyTradePostProc(Long userId, TradePostDetailDTO pDTO) throws Exception {

        log.info("{}.deleteMyTradePostProc Start!", this.getClass().getName());

        Map<String, Object> rMap =
                new HashMap<>();

        if (userId == null) {

            rMap.put("result", "login");
            rMap.put("msg", "로그인이 필요합니다.");

            return rMap;
        }

        if (pDTO == null || pDTO.tradePostId() == null) {

            rMap.put("result", "fail");
            rMap.put("msg", "거래글 정보가 없습니다.");

            return rMap;
        }

        TradePostDetailDTO deleteDTO = TradePostDetailDTO.builder()
                .tradePostId(pDTO.tradePostId())
                .sellerUserId(userId)
                .build();

        int res =
                myPageMapper.deleteMyTradePost(deleteDTO);

        if (res > 0) {

            rMap.put("result", "success");
            rMap.put("msg", "거래글이 삭제되었습니다.");

        } else {

            rMap.put("result", "fail");
            rMap.put("msg", "거래글 삭제에 실패했습니다.");
        }

        log.info("거래글 삭제 userId : {}, tradePostId : {}, result : {}",
                userId,
                pDTO.tradePostId(),
                res);

        log.info("{}.deleteMyTradePostProc End!", this.getClass().getName());

        return rMap;
    }

}