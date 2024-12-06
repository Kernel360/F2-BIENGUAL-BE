package com.biengual.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redis 캐싱하기 위한 어노테이션
 *
 * @author 문찬욱
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCacheable {

    String key();

    String value();

    long ttl() default 1L;  // ttl 값이 음수면 무한

    TimeUnit timeUnit() default TimeUnit.DAYS;
}
