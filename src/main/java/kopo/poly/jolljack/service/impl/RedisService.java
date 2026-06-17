package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.service.IRedisService;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService implements IRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String EMAIL_VERIFY_KEY_PREFIX = "email:verify:";
    private static final long EMAIL_VERIFY_TTL_SECONDS = 180;

    @Override
    public void setEmailVerifyCode(String purpose, String email, String code) throws Exception {

        log.info("{}.setEmailVerifyCode Start!", this.getClass().getName());

        String key = getEmailVerifyKey(purpose, email);

        redisTemplate.opsForValue().set(
                key,
                CmmUtil.nvl(code),
                Duration.ofSeconds(EMAIL_VERIFY_TTL_SECONDS)
        );

        log.info("Redis Email Verify Key : {}", key);
        log.info("{}.setEmailVerifyCode End!", this.getClass().getName());
    }

    @Override
    public String getEmailVerifyCode(String purpose, String email) throws Exception {

        log.info("{}.getEmailVerifyCode Start!", this.getClass().getName());

        String key = getEmailVerifyKey(purpose, email);
        String code = CmmUtil.nvl(redisTemplate.opsForValue().get(key));

        log.info("Redis Email Verify Key : {}", key);
        log.info("{}.getEmailVerifyCode End!", this.getClass().getName());

        return code;
    }

    @Override
    public void deleteEmailVerifyCode(String purpose, String email) throws Exception {

        log.info("{}.deleteEmailVerifyCode Start!", this.getClass().getName());

        String key = getEmailVerifyKey(purpose, email);

        redisTemplate.delete(key);

        log.info("Redis Email Verify Key Delete : {}", key);
        log.info("{}.deleteEmailVerifyCode End!", this.getClass().getName());
    }

    @Override
    public long getEmailVerifyCodeTtl(String purpose, String email) throws Exception {

        log.info("{}.getEmailVerifyCodeTtl Start!", this.getClass().getName());

        String key = getEmailVerifyKey(purpose, email);

        Long ttl = redisTemplate.getExpire(key);

        log.info("Redis Email Verify Key : {}", key);
        log.info("Redis Email Verify TTL : {}", ttl);
        log.info("{}.getEmailVerifyCodeTtl End!", this.getClass().getName());

        return ttl == null ? -2 : ttl;
    }

    private String getEmailVerifyKey(String purpose, String email) {

        return EMAIL_VERIFY_KEY_PREFIX
                + CmmUtil.nvl(purpose)
                + ":"
                + CmmUtil.nvl(email);
    }
}