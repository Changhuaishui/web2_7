package org.example.web2_7.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UlidUtilsTest {

    @Test
    void testGenerateUlid() {
        // 生成ULID
        String ulid = UlidUtils.generate();
        
        // 验证ULID格式符合要求：
        // 1. 长度应该是26个字符
        assertEquals(26, ulid.length(), "ULID长度应该是26个字符");
        
        // 2. 字符应该是Crockford's Base32（A-Z除了I,L,O加上0-9）
        assertTrue(ulid.matches("[0123456789ABCDEFGHJKMNPQRSTVWXYZ]{26}"), 
                "ULID应该只包含有效的Crockford's Base32字符");
    }

    @Test
    void testUlidUniqueness() {
        // 生成多个ULID并验证唯一性
        int count = 1000;
        Set<String> ulidSet = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            String ulid = UlidUtils.generate();
            assertFalse(ulidSet.contains(ulid), "生成的ULID不应重复");
            ulidSet.add(ulid);
        }
        
        assertEquals(count, ulidSet.size(), "应该生成指定数量的唯一ULID");
    }

    @Test
    void testGenerateWithTimestamp() {
        // 使用不同时间戳生成ULID
        long timestamp1 = System.currentTimeMillis();
        String ulid1 = UlidUtils.generateWithTimestamp(timestamp1);
        
        // 等待一段时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long timestamp2 = System.currentTimeMillis();
        String ulid2 = UlidUtils.generateWithTimestamp(timestamp2);
        
        // 验证两个ULID不同
        assertNotEquals(ulid1, ulid2, "不同时间戳生成的ULID应该不同");
        
        // 验证基于时间排序特性（前10个字符是时间戳部分）
        String timestamp1Part = ulid1.substring(0, 10);
        String timestamp2Part = ulid2.substring(0, 10);
        
        // 确保时间部分按照时间递增
        assertTrue(timestamp1Part.compareTo(timestamp2Part) < 0, 
                "后生成的ULID的时间部分应该大于先生成的");
    }
} 