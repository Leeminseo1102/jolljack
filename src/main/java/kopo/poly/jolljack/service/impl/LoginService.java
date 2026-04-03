package kopo.poly.jolljack.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.jolljack.dto.UserDTO;
import kopo.poly.jolljack.mapper.ILoginMapper;
import kopo.poly.jolljack.service.ILoginService;
import kopo.poly.jolljack.util.CmmUtil;
import kopo.poly.jolljack.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements ILoginService {

    private final ILoginMapper loginMapper;

    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_LOGIN_ID = "loginId";
    private static final String SESSION_USER_NAME = "userName";
    private static final String SESSION_USER_EMAIL = "userEmail";
    private static final String SESSION_REGION_ID = "regionId";

    @Override
    public Map<String, Object> loginProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());

        String loginId = CmmUtil.nvl(request.getParameter("loginId"));
        String password = CmmUtil.nvl(request.getParameter("password"));

        Map<String, Object> rMap = new HashMap<>();

        if (loginId.isEmpty()) {
            rMap.put("result", "LOGINID_EMPTY");
            rMap.put("msg", getMsg("LOGINID_EMPTY"));
            return rMap;
        }

        if (password.isEmpty()) {
            rMap.put("result", "PASSWORD_EMPTY");
            rMap.put("msg", getMsg("PASSWORD_EMPTY"));
            return rMap;
        }

        String passwordHash = EncryptUtil.encHashSHA256(password);

        UserDTO pDTO = UserDTO.builder()
                .loginId(loginId)
                .passwordHash(passwordHash)
                .build();

        UserDTO loginUser = loginMapper.getLoginUser(pDTO);

        if (loginUser == null) {
            rMap.put("result", "LOGIN_FAIL");
            rMap.put("msg", getMsg("LOGIN_FAIL"));
            return rMap;
        }

        session.setAttribute(SESSION_USER_ID, loginUser.getUserId());
        session.setAttribute(SESSION_LOGIN_ID, loginUser.getLoginId());
        session.setAttribute(SESSION_USER_NAME, loginUser.getName());
        session.setAttribute(SESSION_USER_EMAIL, loginUser.getEmail());
        session.setAttribute(SESSION_REGION_ID, loginUser.getRegionId());
        session.setMaxInactiveInterval(3600);

        UserDTO updateDTO = UserDTO.builder()
                .userId(loginUser.getUserId())
                .build();

        loginMapper.updateLastLoginAt(updateDTO);

        rMap.put("result", "LOGIN_OK");
        rMap.put("msg", getMsg("LOGIN_OK"));

        log.info("{}.loginProc End!", this.getClass().getName());

        return rMap;
    }

    @Override
    public void logoutProc(HttpSession session) throws Exception {

        log.info("{}.logoutProc Start!", this.getClass().getName());

        session.invalidate();

        log.info("{}.logoutProc End!", this.getClass().getName());
    }

    private String getMsg(String code) {
        return switch (code) {
            case "LOGINID_EMPTY" -> "아이디를 입력해주세요.";
            case "PASSWORD_EMPTY" -> "비밀번호를 입력해주세요.";
            case "LOGIN_FAIL" -> "아이디 또는 비밀번호가 올바르지 않습니다.";
            case "LOGIN_OK" -> "로그인이 완료되었습니다.";
            default -> "오류가 발생했습니다.";
        };
    }
}