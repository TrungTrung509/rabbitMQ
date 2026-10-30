package testmq.demo.service.cache.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import testmq.demo.service.cache.RedisCacheService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisCacheServiceImpl implements RedisCacheService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;


    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return type.cast(value);
    }

    public <T> T getList(String key, TypeReference<T> type){
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;

        return objectMapper.convertValue(value, type);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public Boolean exists(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, Duration ttl) {

    }
}
