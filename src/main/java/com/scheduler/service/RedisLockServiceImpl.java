package com.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockServiceImpl implements RedisLockService {

    private static final ThreadLocal<Map<String, String>> LOCK_TOKENS =
            ThreadLocal.withInitial(HashMap::new);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>();
    static {
        RELEASE_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
        );
        RELEASE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean acquireLock(String key, long timeoutMillis) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofMillis(timeoutMillis));// saving in redis - key and token
        if (Boolean.TRUE.equals(acquired)) {
            Map<String, String> map = LOCK_TOKENS.get(); // saving in thread local for thread for future reference to release lock - key and token
            map.put(key, token);
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock(String key) {
        Map<String, String> map = LOCK_TOKENS.get();
        String token = map != null ? map.remove(key) : null;
        if (token == null) {
            log.debug("No lock token held for key={}, skipping release", key);
            return;
        }
        try {
            Long deleted = stringRedisTemplate.execute(
                    RELEASE_SCRIPT,
                    Collections.singletonList(key),
                    token
            );
            if (deleted != null && deleted > 0) {
                log.debug("Redis lock deleted: key={}", key);
            } else {
                log.debug("Redis lock not deleted (expired or owner changed): key={}", key);
            }
        } finally {
            if (map.isEmpty()) {
                LOCK_TOKENS.remove();
            }
        }
    }
}
