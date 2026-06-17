package kopo.poly.jolljack.service;

public interface IRedisService {

    void setEmailVerifyCode(String purpose, String email, String code) throws Exception;

    String getEmailVerifyCode(String purpose, String email) throws Exception;

    void deleteEmailVerifyCode(String purpose, String email) throws Exception;

    long getEmailVerifyCodeTtl(String purpose, String email) throws Exception;

}