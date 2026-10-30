package testmq.demo.service.cache;


import tools.jackson.core.type.TypeReference;

import java.time.Duration;

public interface RedisCacheService {
    void set(String key, Object T, Duration ttl);
    <T> T get(String key, Class<T> type);
    void delete(String key);
    Boolean exists(String key);
    void expire(String key, Duration ttl);

    <T> T getList(String key, TypeReference<T> type);
}
