package org.example.web2_7.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ------弃用--------
 * 为后来准备，暂时用不上
 * 中文搜索辅助工具类
 * 提供中文关键词处理和优化搜索的功能
 */
public class ChineseSearchHelper {
    private static final Logger logger = LoggerFactory.getLogger(ChineseSearchHelper.class);
    
    // Lucene特殊字符
    private static final Pattern LUCENE_SPECIAL_CHARS = Pattern.compile("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/]");
    
    /**
     * 预处理中文关键词
     * @param keyword 原始关键词
     * @return 处理后的关键词
     */
    public static String preprocessChineseKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        
        keyword = keyword.trim();
        
        // 转义Lucene特殊字符
        keyword = escapeLuceneSpecialChars(keyword);
        
        try {
            // 使用IK分词器进行分词
            StringReader reader = new StringReader(keyword);
            IKSegmenter segmenter = new IKSegmenter(reader, true); // 启用智能分词
            List<String> tokens = new ArrayList<>();
            Lexeme lexeme;
            
            while ((lexeme = segmenter.next()) != null) {
                tokens.add(lexeme.getLexemeText());
            }
            
            // 处理短词和长词
            StringBuilder result = new StringBuilder();
            for (String token : tokens) {
                if (token.length() <= 3) {
                    // 对于短词，保持原样
                    result.append(token).append(" ");
                } else {
                    // 对于长词，添加原词和分词结果
                    result.append(token).append(" ");
                    // 添加2-gram分词结果
                    for (int i = 0; i < token.length() - 1; i++) {
                        result.append(token.substring(i, i + 2)).append(" ");
                    }
                }
            }
            
            String processedKeyword = result.toString().trim();
            logger.debug("关键词处理: {} -> {}", keyword, processedKeyword);
            return processedKeyword;
            
        } catch (Exception e) {
            logger.error("关键词处理失败: {}", keyword, e);
            return keyword;
        }
    }
    
    /**
     * 转义Lucene特殊字符
     */
    private static String escapeLuceneSpecialChars(String keyword) {
        return LUCENE_SPECIAL_CHARS.matcher(keyword).replaceAll("\\\\$0");
    }
    
    /**
     * 获取中文关键词的同义词扩展
     * @param keyword 原始关键词
     * @return 扩展后的关键词列表
     */
    public static List<String> getSynonyms(String keyword) {
        // TODO: 实现同义词扩展功能
        // 可以通过接入同义词词典或调用外部API实现
        List<String> synonyms = new ArrayList<>();
        synonyms.add(keyword);
        return synonyms;
    }
} 