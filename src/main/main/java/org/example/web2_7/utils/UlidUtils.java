package org.example.web2_7.utils;

import de.huxhorn.sulky.ulid.ULID;

/**
 * ULID 工具类
 * 用于生成唯一ID，替代UUID算法
 * ULID (Universally Unique Lexicographically Sortable Identifier) 特点：
 * 1. 128位兼容UUID
 * 2. 按照时间顺序可排序
 * 3. 使用Crockford's Base32编码（每个字符5比特，共26个字符）
 * 4. 大小写不敏感，易读
 * 5. 无特殊字符，URL安全
 * 格式：26个字符，前10个时间戳，后16个随机数
 */
public class UlidUtils {
    private static final ULID ulid = new ULID();

    /**
     * 生成ULID字符串
     * @return 26个字符的ULID字符串
     */
    public static String generate() {
        return ulid.nextULID();
    }

    /**
     * 根据指定时间戳生成ULID
     * 通常用于测试或特定场景
     * @param timestamp 时间戳（毫秒）
     * @return 26个字符的ULID字符串
     */
    public static String generateWithTimestamp(long timestamp) {
        return ulid.nextValue(timestamp).toString();
    }
} 