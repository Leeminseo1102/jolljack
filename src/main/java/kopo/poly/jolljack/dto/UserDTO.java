package kopo.poly.jolljack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {

    // 회원번호 (PK)
    private Long userId;

    // 로그인 아이디
    private String loginId;

    // 비밀번호 해시 (SHA-256 암호화된 값 - DB 저장용)
    private String passwordHash;

    // 이름
    private String name;

    // 이메일
    private String email;

    // 지역 아이디 (FK → region.region_id)
    private Long regionId;

    // 선호 작물 아이디 (FK → crop.crop_id)
    private Long favoriteCropId;

    // 회원 상태 (ACTIVE / DELETED)
    private String status;

    // 가입일시
    private String createdAt;

    // 수정일시
    private String updatedAt;

    // 최근 로그인 일시
    private String lastLoginAt;

    // 탈퇴일시
    private String deletedAt;

//    // -----------------------------------------------
//    // DB 컬럼 외 - 비즈니스 로직용 필드
//    // -----------------------------------------------
//
    // 이메일 인증코드 (Redis 저장/검증용)
    private String emailCode;

    // 비밀번호 확인 (회원가입 시 클라이언트 입력값 검증용)
    private String passwordConfirm;

}