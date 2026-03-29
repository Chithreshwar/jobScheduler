package com.scheduler.service;

public interface RedisLockService {

    /**
     * Attempts to acquire a lock using SET NX with expiry.
     *
     * @param key            Redis key
     * @param timeoutMillis  lock TTL (PX); key is auto-deleted after this duration
     * @return true if this caller acquired the lock
     */
    boolean acquireLock(String key, long timeoutMillis);

    /**
     * Releases the lock only if it is still owned by this thread (same token as acquire).
     */
    void releaseLock(String key);
}
