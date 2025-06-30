package com.flab.tableorder.util;

public class RedisUtil {
    public static String getRedisKey(String... keys) {
        return String.join(":", keys);
    }
}
