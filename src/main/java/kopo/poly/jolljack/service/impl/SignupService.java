package kopo.poly.jolljack.service.impl;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.CropDTO;
import kopo.poly.jolljack.dto.RegionDTO;
import kopo.poly.jolljack.dto.UserDTO;
import kopo.poly.jolljack.mapper.ISignupMapper;
import kopo.poly.jolljack.service.ISignupService;
import kopo.poly.jolljack.util.CmmUtil;
import kopo.poly.jolljack.util.EncryptUtil;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService implements ISignupService {


    @Value("${spring.mail.username}")
    private String fromMail;

    private final ISignupMapper signupMapper;
    private final JavaMailSender mailSender;

    private static final String SESSION_SIGNUP_NAME = "signupName";
    private static final String SESSION_SIGNUP_EMAIL = "signupEmail";
    private static final String SESSION_EMAIL_CODE = "emailCode";
    private static final String SESSION_EMAIL_CODE_EXPIRE_TIME = "emailCodeExpireTime";
    private static final String SESSION_EMAIL_VERIFIED = "emailVerified";
    private static final String SESSION_SIGNUP_LOGIN_ID = "signupLoginId";
    private static final String SESSION_SIGNUP_PASSWORD = "signupPassword";

    @Override
    public Map<String, Object> sendEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        String name = CmmUtil.nvl(request.getParameter("name"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        Map<String, Object> rMap = new HashMap<>();

        if (name.isEmpty()) {
            rMap.put("result", "NAME_EMPTY");
            rMap.put("msg", getMsg("NAME_EMPTY"));
            return rMap;
        }

        UserDTO pDTO = UserDTO.builder()
                .email(email)
                .build();

        String emailResult = checkEmailValid(pDTO);

        if (!"EMAIL_OK".equals(emailResult)) {
            rMap.put("result", emailResult);
            rMap.put("msg", getMsg(emailResult));
            return rMap;
        }

        String code = createAuthCode();

        sendAuthMail(email, code);

        session.setAttribute(SESSION_SIGNUP_NAME, name);
        session.setAttribute(SESSION_SIGNUP_EMAIL, email);
        session.setAttribute(SESSION_EMAIL_CODE, code);
        session.setAttribute(SESSION_EMAIL_CODE_EXPIRE_TIME, System.currentTimeMillis() + (3 * 60 * 1000L));
        session.removeAttribute(SESSION_EMAIL_VERIFIED);

        rMap.put("result", "SEND_OK");
        rMap.put("msg", getMsg("SEND_OK"));

        return rMap;
    }

    @Override
    public Map<String, Object> verifyEmailCodeProc(HttpServletRequest request, HttpSession session) throws Exception {

        String inputCode = CmmUtil.nvl(request.getParameter("inputCode"));
        String savedCode = CmmUtil.nvl((String) session.getAttribute(SESSION_EMAIL_CODE));
        Long expireTime = (Long) session.getAttribute(SESSION_EMAIL_CODE_EXPIRE_TIME);

        String result = verifyEmailCode(inputCode, savedCode, expireTime);

        Map<String, Object> rMap = new HashMap<>();
        rMap.put("result", result);
        rMap.put("msg", getMsg(result));

        if ("CODE_OK".equals(result)) {
            session.setAttribute(SESSION_EMAIL_VERIFIED, true);
            session.removeAttribute(SESSION_EMAIL_CODE);
            session.removeAttribute(SESSION_EMAIL_CODE_EXPIRE_TIME);
        }

        return rMap;
    }

    @Override
    public String checkStep1Session(HttpSession session) throws Exception {

        String signupName = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_NAME));
        String signupEmail = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_EMAIL));
        Boolean emailVerified = (Boolean) session.getAttribute(SESSION_EMAIL_VERIFIED);

        if (signupName.isEmpty()) {
            return "STEP1_INVALID";
        }

        if (signupEmail.isEmpty()) {
            return "STEP1_INVALID";
        }

        if (emailVerified == null || !emailVerified) {
            return "STEP1_INVALID";
        }

        return "STEP1_OK";
    }

    @Override
    public Map<String, Object> checkLoginIdProc(HttpServletRequest request) throws Exception {

        String loginId = CmmUtil.nvl(request.getParameter("loginId"));

        UserDTO pDTO = UserDTO.builder()
                .loginId(loginId)
                .build();

        String result = checkLoginIdValid(pDTO);

        Map<String, Object> rMap = new HashMap<>();
        rMap.put("result", result);
        rMap.put("msg", getMsg(result));

        return rMap;
    }

    @Override
    public Map<String, Object> saveStep2Proc(HttpServletRequest request, HttpSession session) throws Exception {

        Map<String, Object> rMap = new HashMap<>();

        String step1Result = checkStep1Session(session);

        if (!"STEP1_OK".equals(step1Result)) {
            rMap.put("result", "STEP1_INVALID");
            rMap.put("msg", getMsg("STEP1_INVALID"));
            return rMap;
        }

        String loginId = CmmUtil.nvl(request.getParameter("loginId"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String passwordConfirm = CmmUtil.nvl(request.getParameter("passwordConfirm"));

        UserDTO pDTO = UserDTO.builder()
                .loginId(loginId)
                .passwordHash(password)
                .passwordConfirm(passwordConfirm)
                .build();

        String result = validateStep2(pDTO);

        if ("STEP2_OK".equals(result)) {
            session.setAttribute(SESSION_SIGNUP_LOGIN_ID, loginId);
            session.setAttribute(SESSION_SIGNUP_PASSWORD, password);
        }

        rMap.put("result", result);
        rMap.put("msg", getMsg(result));

        return rMap;
    }

    @Override
    public String checkStep2Session(HttpSession session) throws Exception {

        String step1Result = checkStep1Session(session);
        String signupLoginId = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_LOGIN_ID));
        String signupPassword = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_PASSWORD));

        if (!"STEP1_OK".equals(step1Result)) {
            return "STEP2_INVALID";
        }

        if (signupLoginId.isEmpty()) {
            return "STEP2_INVALID";
        }

        if (signupPassword.isEmpty()) {
            return "STEP2_INVALID";
        }

        return "STEP2_OK";
    }

    @Override
    public List<RegionDTO> getSidoList() throws Exception {
        return signupMapper.getSidoList();
    }

    @Override
    public List<RegionDTO> getSigunguListProc(HttpServletRequest request) throws Exception {

        String sidoName = CmmUtil.nvl(request.getParameter("sidoName"));

        RegionDTO pDTO = RegionDTO.builder()
                .sidoName(sidoName)
                .build();

        return signupMapper.getSigunguList(pDTO);
    }

    @Override
    public List<CropDTO> getCropList() throws Exception {
        return signupMapper.getCropList();
    }

    @Override
    public Map<String, Object> signupCompleteProc(HttpServletRequest request, HttpSession session) throws Exception {

        Map<String, Object> rMap = new HashMap<>();

        String step2Result = checkStep2Session(session);

        if (!"STEP2_OK".equals(step2Result)) {
            rMap.put("result", "SESSION_INVALID");
            rMap.put("msg", getMsg("SESSION_INVALID"));
            return rMap;
        }

        String sidoName = CmmUtil.nvl(request.getParameter("sidoName"));
        String sigunguName = CmmUtil.nvl(request.getParameter("sigunguName"));
        String favoriteCropId = CmmUtil.nvl(request.getParameter("favoriteCropId"));

        if (sidoName.isEmpty() || sigunguName.isEmpty()) {
            rMap.put("result", "REGION_EMPTY");
            rMap.put("msg", getMsg("REGION_EMPTY"));
            return rMap;
        }

        String loginId = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_LOGIN_ID));
        String password = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_PASSWORD));
        String name = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_NAME));
        String email = CmmUtil.nvl((String) session.getAttribute(SESSION_SIGNUP_EMAIL));

        RegionDTO regionPDTO = RegionDTO.builder()
                .sidoName(sidoName)
                .sigunguName(sigunguName)
                .build();

        RegionDTO regionDTO = signupMapper.getRegionId(regionPDTO);

        if (regionDTO == null || regionDTO.getRegionId() == null) {
            rMap.put("result", "REGION_EMPTY");
            rMap.put("msg", getMsg("REGION_EMPTY"));
            return rMap;
        }

        String passwordHash = EncryptUtil.encHashSHA256(password);

        UserDTO pDTO = UserDTO.builder()
                .loginId(loginId)
                .passwordHash(passwordHash)
                .name(name)
                .email(email)
                .regionId(regionDTO.getRegionId())
                .favoriteCropId(favoriteCropId.isEmpty() ? null : Long.parseLong(favoriteCropId))
                .build();

        int res = signupMapper.insertUser(pDTO);

        if (res > 0) {
            clearSignupSession(session);
            rMap.put("result", "SIGNUP_OK");
            rMap.put("msg", getMsg("SIGNUP_OK"));
        } else {
            rMap.put("result", "SIGNUP_FAIL");
            rMap.put("msg", getMsg("SIGNUP_FAIL"));
        }

        return rMap;
    }

    private String checkEmailValid(UserDTO pDTO) throws Exception {

        String email = CmmUtil.nvl(pDTO.getEmail());

        if (email.isEmpty()) {
            return "EMAIL_EMPTY";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "EMAIL_INVALID";
        }

        int count = signupMapper.checkEmail(pDTO);

        if (count > 0) {
            return "EMAIL_EXISTS";
        }

        return "EMAIL_OK";
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

    private String checkLoginIdValid(UserDTO pDTO) throws Exception {

        String loginId = CmmUtil.nvl(pDTO.getLoginId());

        if (loginId.isEmpty()) {
            return "LOGINID_EMPTY";
        }

        if (!loginId.matches("^[a-zA-Z0-9]{4,20}$")) {
            return "LOGINID_INVALID";
        }

        int count = signupMapper.checkLoginId(pDTO);

        if (count > 0) {
            return "LOGINID_EXISTS";
        }

        return "LOGINID_OK";
    }

    private String validateStep2(UserDTO pDTO) throws Exception {

        String loginId = CmmUtil.nvl(pDTO.getLoginId());
        String password = CmmUtil.nvl(pDTO.getPasswordHash());
        String passwordConfirm = CmmUtil.nvl(pDTO.getPasswordConfirm());

        if (loginId.isEmpty()) {
            return "LOGINID_EMPTY";
        }

        if (!loginId.matches("^[a-zA-Z0-9]{4,20}$")) {
            return "LOGINID_INVALID";
        }

        int count = signupMapper.checkLoginId(UserDTO.builder().loginId(loginId).build());

        if (count > 0) {
            return "LOGINID_EXISTS";
        }

        if (password.isEmpty()) {
            return "PASSWORD_EMPTY";
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=]{8,20}$")) {
            return "PASSWORD_INVALID";
        }

        if (passwordConfirm.isEmpty()) {
            return "PASSWORD_CONFIRM_EMPTY";
        }

        if (!password.equals(passwordConfirm)) {
            return "PASSWORD_MISMATCH";
        }

        return "STEP2_OK";
    }

    private String createAuthCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private void sendAuthMail(String email, String code) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromMail);
        helper.setTo(email);
        helper.setSubject("[AGRIWATCH] 이메일 인증코드 안내");
        helper.setText(
                "<div style='font-family:Arial; font-size:14px;'>" +
                        "<h3>이메일 인증코드</h3>" +
                        "<p>아래 인증코드를 입력해주세요. (3분 이내)</p>" +
                        "<h2 style='color:#2e7d32; letter-spacing:4px;'>" + code + "</h2>" +
                        "</div>",
                true
        );

        mailSender.send(message);
    }

    private void clearSignupSession(HttpSession session) {
        session.removeAttribute(SESSION_SIGNUP_NAME);
        session.removeAttribute(SESSION_SIGNUP_EMAIL);
        session.removeAttribute(SESSION_EMAIL_CODE);
        session.removeAttribute(SESSION_EMAIL_CODE_EXPIRE_TIME);
        session.removeAttribute(SESSION_EMAIL_VERIFIED);
        session.removeAttribute(SESSION_SIGNUP_LOGIN_ID);
        session.removeAttribute(SESSION_SIGNUP_PASSWORD);
    }

    private String getMsg(String code) {
        return switch (code) {
            case "NAME_EMPTY" -> "이름을 입력해주세요.";
            case "EMAIL_EMPTY" -> "이메일을 입력해주세요.";
            case "EMAIL_INVALID" -> "이메일 형식이 올바르지 않습니다.";
            case "EMAIL_EXISTS" -> "이미 가입된 이메일입니다.";
            case "SEND_OK" -> "인증코드가 발송되었습니다.";
            case "CODE_EMPTY" -> "인증코드를 입력해주세요.";
            case "CODE_EXPIRED" -> "인증시간이 만료되었습니다.";
            case "CODE_MISMATCH" -> "인증코드가 일치하지 않습니다.";
            case "CODE_OK" -> "이메일 인증이 완료되었습니다.";
            case "STEP1_INVALID" -> "1단계 회원가입 정보가 올바르지 않습니다.";
            case "LOGINID_EMPTY" -> "아이디를 입력해주세요.";
            case "LOGINID_INVALID" -> "아이디는 4~20자의 영문, 숫자만 입력 가능합니다.";
            case "LOGINID_EXISTS" -> "이미 사용 중인 아이디입니다.";
            case "LOGINID_OK" -> "사용 가능한 아이디입니다.";
            case "PASSWORD_EMPTY" -> "비밀번호를 입력해주세요.";
            case "PASSWORD_INVALID" -> "비밀번호는 8~20자의 영문, 숫자를 포함해야 합니다.";
            case "PASSWORD_CONFIRM_EMPTY" -> "비밀번호 확인을 입력해주세요.";
            case "PASSWORD_MISMATCH" -> "비밀번호가 일치하지 않습니다.";
            case "STEP2_OK" -> "2단계 입력이 완료되었습니다.";
            case "SESSION_INVALID" -> "회원가입 세션 정보가 유효하지 않습니다.";
            case "REGION_EMPTY" -> "지역을 선택해주세요.";
            case "SIGNUP_OK" -> "회원가입이 완료되었습니다.";
            case "SIGNUP_FAIL" -> "회원가입 처리 중 오류가 발생했습니다.";
            default -> "오류가 발생했습니다.";
        };
    }
}