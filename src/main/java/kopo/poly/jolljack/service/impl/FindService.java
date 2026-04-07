package kopo.poly.jolljack.service.impl;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.UserDTO;
import kopo.poly.jolljack.mapper.IFindMapper;
import kopo.poly.jolljack.service.IFindService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import kopo.poly.jolljack.util.EncryptUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindService implements IFindService {

    @Value("${spring.mail.username}")
    private String fromMail;

    private final IFindMapper findMapper;
    private final JavaMailSender mailSender;

    private static final String SESSION_FIND_NAME = "findName";
    private static final String SESSION_FIND_EMAIL = "findEmail";
    private static final String SESSION_FIND_CODE = "findCode";
    private static final String SESSION_FIND_CODE_EXPIRE_TIME = "findCodeExpireTime";
    private static final String SESSION_FIND_VERIFIED = "findVerified";
    private static final String SESSION_FIND_PW_USER_ID = "findPwUserId";
    private static final String SESSION_FIND_PW_LOGIN_ID = "findPwLoginId";
    private static final String SESSION_FIND_PW_NAME = "findPwName";
    private static final String SESSION_FIND_PW_EMAIL = "findPwEmail";
    private static final String SESSION_FIND_PW_CODE = "findPwCode";
    private static final String SESSION_FIND_PW_CODE_EXPIRE_TIME = "findPwCodeExpireTime";
    private static final String SESSION_FIND_PW_VERIFIED = "findPwVerified";

    @Override
    public Map<String, Object> sendFindIdEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.sendFindIdEmailCodeProc Start!", this.getClass().getName());

        String name = CmmUtil.nvl(request.getParameter("name"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        Map<String, Object> rMap = new HashMap<>();

        if (name.isEmpty()) {
            rMap.put("result", "NAME_EMPTY");
            rMap.put("msg", getMsg("NAME_EMPTY"));
            return rMap;
        }

        if (email.isEmpty()) {
            rMap.put("result", "EMAIL_EMPTY");
            rMap.put("msg", getMsg("EMAIL_EMPTY"));
            return rMap;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            rMap.put("result", "EMAIL_INVALID");
            rMap.put("msg", getMsg("EMAIL_INVALID"));
            return rMap;
        }

        UserDTO pDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .build();

        UserDTO rDTO = findMapper.getUserByNameAndEmail(pDTO);

        if (rDTO == null) {
            rMap.put("result", "FIND_ID_FAIL");
            rMap.put("msg", getMsg("FIND_ID_FAIL"));
            return rMap;
        }

        String code = createAuthCode();

        sendAuthMail(email, code);

        session.setAttribute(SESSION_FIND_NAME, name);
        session.setAttribute(SESSION_FIND_EMAIL, email);
        session.setAttribute(SESSION_FIND_CODE, code);
        session.setAttribute(SESSION_FIND_CODE_EXPIRE_TIME, System.currentTimeMillis() + (3 * 60 * 1000L));
        session.removeAttribute(SESSION_FIND_VERIFIED);

        rMap.put("result", "SEND_OK");
        rMap.put("msg", getMsg("SEND_OK"));

        log.info("{}.sendFindIdEmailCodeProc End!", this.getClass().getName());

        return rMap;
    }

    @Override
    public Map<String, Object> verifyFindIdEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.verifyFindIdEmailCodeProc Start!", this.getClass().getName());

        String inputCode = CmmUtil.nvl(request.getParameter("inputCode"));
        String savedCode = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_CODE));
        Long expireTime = (Long) session.getAttribute(SESSION_FIND_CODE_EXPIRE_TIME);

        String result = verifyEmailCode(inputCode, savedCode, expireTime);

        Map<String, Object> rMap = new HashMap<>();
        rMap.put("result", result);
        rMap.put("msg", getMsg(result));

        if ("CODE_OK".equals(result)) {
            session.setAttribute(SESSION_FIND_VERIFIED, true);
            session.removeAttribute(SESSION_FIND_CODE);
            session.removeAttribute(SESSION_FIND_CODE_EXPIRE_TIME);
        }

        log.info("{}.verifyFindIdEmailCodeProc End!", this.getClass().getName());

        return rMap;
    }

    @Override
    public Map<String, Object> findLoginIdProc(HttpSession session) throws Exception {

        log.info("{}.findLoginIdProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        String name = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_NAME));
        String email = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_EMAIL));
        Boolean verified = (Boolean) session.getAttribute(SESSION_FIND_VERIFIED);

        if (name.isEmpty() || email.isEmpty()) {
            rMap.put("result", "SESSION_INVALID");
            rMap.put("msg", getMsg("SESSION_INVALID"));
            return rMap;
        }

        if (verified == null || !verified) {
            rMap.put("result", "VERIFY_REQUIRED");
            rMap.put("msg", getMsg("VERIFY_REQUIRED"));
            return rMap;
        }

        UserDTO pDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .build();

        UserDTO rDTO = findMapper.getUserByNameAndEmail(pDTO);

        if (rDTO == null) {
            rMap.put("result", "FIND_ID_FAIL");
            rMap.put("msg", getMsg("FIND_ID_FAIL"));
            clearFindIdSession(session);
            return rMap;
        }

        rMap.put("result", "FIND_ID_OK");
        rMap.put("msg", getMsg("FIND_ID_OK"));
        rMap.put("loginId", maskLoginId(rDTO.getLoginId()));

        clearFindIdSession(session);

        log.info("{}.findLoginIdProc End!", this.getClass().getName());

        return rMap;
    }

    private String verifyEmailCode(String inputCode, String savedCode, Long expireTime) {

        if (CmmUtil.nvl(inputCode).isEmpty()) {
            return "CODE_EMPTY";
        }

        if (expireTime == null || System.currentTimeMillis() > expireTime) {
            return "CODE_EXPIRED";
        }

        if (CmmUtil.nvl(savedCode).isEmpty()) {
            return "CODE_MISMATCH";
        }

        if (!savedCode.equals(inputCode.trim())) {
            return "CODE_MISMATCH";
        }

        return "CODE_OK";
    }

    private String createAuthCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private void sendAuthMail(String email, String code) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[AGRIWATCH] 아이디 찾기 인증코드 안내");
        helper.setText(
                "<div style='font-family:Arial; font-size:14px;'>" +
                        "<h3>아이디 찾기 인증코드</h3>" +
                        "<p>아래 인증코드를 입력해주세요. (3분 이내)</p>" +
                        "<h2 style='color:#2e7d32; letter-spacing:4px;'>" + code + "</h2>" +
                        "</div>",
                true
        );

        mailSender.send(message);
    }

    private String maskLoginId(String loginId) {

        String safeLoginId = CmmUtil.nvl(loginId);

        if (safeLoginId.length() <= 3) {
            return safeLoginId;
        }

        return safeLoginId.substring(0, 3) + "*".repeat(safeLoginId.length() - 3);
    }

    private void clearFindIdSession(HttpSession session) {
        session.removeAttribute(SESSION_FIND_NAME);
        session.removeAttribute(SESSION_FIND_EMAIL);
        session.removeAttribute(SESSION_FIND_CODE);
        session.removeAttribute(SESSION_FIND_CODE_EXPIRE_TIME);
        session.removeAttribute(SESSION_FIND_VERIFIED);
    }

    //===============================비번==================================//


    @Override
    public Map<String, Object> sendFindPwEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.sendFindPwEmailCodeProc Start!", this.getClass().getName());

        String loginId = CmmUtil.nvl(request.getParameter("loginId"));
        String name = CmmUtil.nvl(request.getParameter("name"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        Map<String, Object> rMap = new HashMap<>();

        if (loginId.isEmpty()) {
            rMap.put("result", "LOGINID_EMPTY");
            rMap.put("msg", getMsg("LOGINID_EMPTY"));
            return rMap;
        }

        if (name.isEmpty()) {
            rMap.put("result", "NAME_EMPTY");
            rMap.put("msg", getMsg("NAME_EMPTY"));
            return rMap;
        }

        if (email.isEmpty()) {
            rMap.put("result", "EMAIL_EMPTY");
            rMap.put("msg", getMsg("EMAIL_EMPTY"));
            return rMap;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            rMap.put("result", "EMAIL_INVALID");
            rMap.put("msg", getMsg("EMAIL_INVALID"));
            return rMap;
        }

        UserDTO pDTO = UserDTO.builder()
                .loginId(loginId)
                .name(name)
                .email(email)
                .build();

        UserDTO rDTO = findMapper.getUserByLoginIdAndNameAndEmail(pDTO);

        if (rDTO == null) {
            rMap.put("result", "FIND_PW_FAIL");
            rMap.put("msg", getMsg("FIND_PW_FAIL"));
            return rMap;
        }

        String code = createAuthCode();

        sendAuthMail(email, code);

        session.setAttribute(SESSION_FIND_PW_USER_ID, rDTO.getUserId());
        session.setAttribute(SESSION_FIND_PW_LOGIN_ID, loginId);
        session.setAttribute(SESSION_FIND_PW_NAME, name);
        session.setAttribute(SESSION_FIND_PW_EMAIL, email);
        session.setAttribute(SESSION_FIND_PW_CODE, code);
        session.setAttribute(SESSION_FIND_PW_CODE_EXPIRE_TIME, System.currentTimeMillis() + (3 * 60 * 1000L));
        session.removeAttribute(SESSION_FIND_PW_VERIFIED);

        rMap.put("result", "SEND_OK");
        rMap.put("msg", getMsg("SEND_OK"));

        log.info("{}.sendFindPwEmailCodeProc End!", this.getClass().getName());

        return rMap;
    }

    @Override
    public Map<String, Object> verifyFindPwEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.verifyFindPwEmailCodeProc Start!", this.getClass().getName());

        String inputCode = CmmUtil.nvl(request.getParameter("inputCode"));
        String savedCode = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_PW_CODE));
        Long expireTime = (Long) session.getAttribute(SESSION_FIND_PW_CODE_EXPIRE_TIME);

        String result = verifyEmailCode(inputCode, savedCode, expireTime);

        Map<String, Object> rMap = new HashMap<>();
        rMap.put("result", result);
        rMap.put("msg", getMsg(result));

        if ("CODE_OK".equals(result)) {
            session.setAttribute(SESSION_FIND_PW_VERIFIED, true);
            session.removeAttribute(SESSION_FIND_PW_CODE);
            session.removeAttribute(SESSION_FIND_PW_CODE_EXPIRE_TIME);
        }

        log.info("{}.verifyFindPwEmailCodeProc End!", this.getClass().getName());

        return rMap;
    }

    @Override
    public String checkFindPwVerifiedSession(HttpSession session) throws Exception {

        Long userId = (Long) session.getAttribute(SESSION_FIND_PW_USER_ID);
        String loginId = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_PW_LOGIN_ID));
        String name = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_PW_NAME));
        String email = CmmUtil.nvl((String) session.getAttribute(SESSION_FIND_PW_EMAIL));
        Boolean verified = (Boolean) session.getAttribute(SESSION_FIND_PW_VERIFIED);

        if (userId == null) {
            return "SESSION_INVALID";
        }

        if (loginId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            return "SESSION_INVALID";
        }

        if (verified == null || !verified) {
            return "VERIFY_REQUIRED";
        }

        return "RESET_OK";
    }

    @Override
    public Map<String, Object> resetPasswordProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.resetPasswordProc Start!", this.getClass().getName());

        Map<String, Object> rMap = new HashMap<>();

        String sessionCheck = checkFindPwVerifiedSession(session);

        if (!"RESET_OK".equals(sessionCheck)) {
            rMap.put("result", sessionCheck);
            rMap.put("msg", getMsg(sessionCheck));
            return rMap;
        }

        String password = CmmUtil.nvl(request.getParameter("password"));
        String passwordConfirm = CmmUtil.nvl(request.getParameter("passwordConfirm"));
        Long userId = (Long) session.getAttribute(SESSION_FIND_PW_USER_ID);

        if (password.isEmpty()) {
            rMap.put("result", "PASSWORD_EMPTY");
            rMap.put("msg", getMsg("PASSWORD_EMPTY"));
            return rMap;
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=]{8,20}$")) {
            rMap.put("result", "PASSWORD_INVALID");
            rMap.put("msg", getMsg("PASSWORD_INVALID"));
            return rMap;
        }

        if (passwordConfirm.isEmpty()) {
            rMap.put("result", "PASSWORD_CONFIRM_EMPTY");
            rMap.put("msg", getMsg("PASSWORD_CONFIRM_EMPTY"));
            return rMap;
        }

        if (!password.equals(passwordConfirm)) {
            rMap.put("result", "PASSWORD_MISMATCH");
            rMap.put("msg", getMsg("PASSWORD_MISMATCH"));
            return rMap;
        }

        String passwordHash = EncryptUtil.encHashSHA256(password);

        UserDTO userDTO = findMapper.getUserByUserId(
                UserDTO.builder()
                        .userId(userId)
                        .build()
        );

        if (userDTO == null) {
            rMap.put("result", "SESSION_INVALID");
            rMap.put("msg", getMsg("SESSION_INVALID"));
            return rMap;
        }

        if (passwordHash.equals(CmmUtil.nvl(userDTO.getPasswordHash()))) {
            rMap.put("result", "PASSWORD_SAME_AS_OLD");
            rMap.put("msg", getMsg("PASSWORD_SAME_AS_OLD"));
            return rMap;
        }

        UserDTO pDTO = UserDTO.builder()
                .userId(userId)
                .passwordHash(passwordHash)
                .build();

        int res = findMapper.updatePasswordHash(pDTO);

        if (res > 0) {
            clearFindPwSession(session);
            rMap.put("result", "RESET_PW_OK");
            rMap.put("msg", getMsg("RESET_PW_OK"));
        } else {
            rMap.put("result", "RESET_PW_FAIL");
            rMap.put("msg", getMsg("RESET_PW_FAIL"));
        }

        log.info("{}.resetPasswordProc End!", this.getClass().getName());

        return rMap;
    }

    private void clearFindPwSession(HttpSession session) {
        session.removeAttribute(SESSION_FIND_PW_USER_ID);
        session.removeAttribute(SESSION_FIND_PW_LOGIN_ID);
        session.removeAttribute(SESSION_FIND_PW_NAME);
        session.removeAttribute(SESSION_FIND_PW_EMAIL);
        session.removeAttribute(SESSION_FIND_PW_CODE);
        session.removeAttribute(SESSION_FIND_PW_CODE_EXPIRE_TIME);
        session.removeAttribute(SESSION_FIND_PW_VERIFIED);
    }



    private String getMsg(String code) {
        return switch (code) {
            //아이디
            case "NAME_EMPTY" -> "이름을 입력해주세요.";
            case "EMAIL_EMPTY" -> "이메일을 입력해주세요.";
            case "EMAIL_INVALID" -> "이메일 형식이 올바르지 않습니다.";
            case "SEND_OK" -> "인증코드가 발송되었습니다.";
            case "CODE_EMPTY" -> "인증코드를 입력해주세요.";
            case "CODE_EXPIRED" -> "인증시간이 만료되었습니다.";
            case "CODE_MISMATCH" -> "인증코드가 일치하지 않습니다.";
            case "CODE_OK" -> "이메일 인증이 완료되었습니다.";
            case "VERIFY_REQUIRED" -> "이메일 인증을 먼저 완료해주세요.";
            case "SESSION_INVALID" -> "아이디 찾기 세션 정보가 유효하지 않습니다.";
            case "FIND_ID_FAIL" -> "일치하는 회원정보가 없습니다.";
            case "FIND_ID_OK" -> "아이디를 찾았습니다.";
            //비번
            case "LOGINID_EMPTY" -> "아이디를 입력해주세요.";
            case "FIND_PW_FAIL" -> "사용자님의 계정이 존재하지 않습니다.";
            case "PASSWORD_EMPTY" -> "비밀번호를 입력해주세요.";
            case "PASSWORD_INVALID" -> "비밀번호는 8~20자의 영문, 숫자를 포함해야 합니다.";
            case "PASSWORD_CONFIRM_EMPTY" -> "비밀번호 확인을 입력해주세요.";
            case "PASSWORD_MISMATCH" -> "비밀번호가 일치하지 않습니다.";
            case "RESET_PW_OK" -> "비밀번호가 재설정되었습니다.";
            case "RESET_PW_FAIL" -> "비밀번호 재설정 중 오류가 발생했습니다.";
            case "PASSWORD_SAME_AS_OLD" -> "이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.";
            //오류처리
            default -> "오류가 발생했습니다.";
        };
    }
}