package com.i5mc.ban;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.val;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public enum L2Pool {

    INSTANCE;

    private final Cache<String, Object> pool = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final Object invalid = new Object();

    public static <T> void put(String key, T any) {
        INSTANCE.pool.put(key, any);
    }

    @SneakyThrows
    public static <T> T pull(String key, Supplier<T> supplier) {
        val output = INSTANCE.pool.get(key, () -> {
            val value = supplier.get();
            return value == null ? INSTANCE.invalid : value;
        });
        return output == INSTANCE.invalid ? null : (T) output;
    }

    public static void invalid(String key) {
        INSTANCE.pool.invalidate(key);
    }

}
