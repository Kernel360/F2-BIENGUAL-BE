package com.biengual.userapi.aop;

import com.biengual.core.annotation.RedisCacheable;
import com.biengual.core.util.CustomSpringELParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisCacheAspect {
    private static final String REDIS_CACHE_PREFIX = "CACHE:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.biengual.core.annotation.RedisCacheable)")
    private void redisCacheable() {}

    @Around("redisCacheable()")
    public Object redisCacheableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisCacheable redisCacheable = method.getAnnotation(RedisCacheable.class);
        long ttl = redisCacheable.ttl();
        TimeUnit timeUnit = redisCacheable.timeUnit();

        String key = generateKey(joinPoint, signature, redisCacheable);

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return getRedisValue(key, getTypeReference(method));
        }

        Object result = joinPoint.proceed();

        if (ttl < 0) {
            putRedisValue(key, result);
        } else {
            putRedisValue(key, result, ttl, timeUnit);
        }

        return result;
    }

    // Internal Method =================================================================================================

    // 캐싱을 하기 위한 Key 생성
    private String generateKey(
        ProceedingJoinPoint joinPoint, MethodSignature signature, RedisCacheable redisCacheable
    ) {
        return REDIS_CACHE_PREFIX + redisCacheable.value() + ":" +
            CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), redisCacheable.key());
    }

    // Redis key에 대해 value를 get
    private  <T> T getRedisValue(String key, TypeReference<T> typeReference) throws JsonProcessingException {
        String redisValue = redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(redisValue)) {
            return null;
        }else{
            return objectMapper.readValue(redisValue, typeReference);
        }
    }

    // ttl 적용하여 Redis key에 대해 value를 put
    private void putRedisValue(String key, Object classType, long ttl, TimeUnit timeUnit) throws JsonProcessingException {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(classType), ttl, timeUnit);
    }

    // ttl 적용하지 않고 Redis key에 대해 value를 put
    private void putRedisValue(String key, Object classType) throws JsonProcessingException {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(classType));
    }

    // TypeReference를 이용한 반환 타입 전달
    private TypeReference<?> getTypeReference(Method method) {
        if (method.getReturnType().equals(Set.class)) {
            return new TypeReference<Set<?>>() {};
        }

        return new TypeReference<>() {};
    }
}
