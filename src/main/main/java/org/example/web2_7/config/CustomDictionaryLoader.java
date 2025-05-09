package org.example.web2_7.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.dic.Dictionary;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomDictionaryLoader {
    private static final Logger logger = LoggerFactory.getLogger(CustomDictionaryLoader.class);
    
    private final String dictionaryPath;
    private final Dictionary dictionary;
    
    @Autowired
    public CustomDictionaryLoader(String customDictionaryPath, Dictionary dictionary) {
        this.dictionaryPath = customDictionaryPath;
        this.dictionary = dictionary;
    }
    
    @PostConstruct
    public void init() {
        loadCustomDictionary();
    }
    
    /**
     * 加载自定义词典
     */
    private void loadCustomDictionary() {
        try {
            Path customDictPath = Paths.get(dictionaryPath, "custom.dic");
            Path stopwordsDictPath = Paths.get(dictionaryPath, "stopwords.dic");
            
            // 加载自定义词典
            List<String> customWords = readDictionaryFile(customDictPath);
            logger.info("加载{}个自定义词条", customWords.size());
            
            // 加载停用词词典
            List<String> stopwords = readDictionaryFile(stopwordsDictPath);
            logger.info("加载{}个停用词", stopwords.size());
            
            // 更新IK分词器词典
            if (!customWords.isEmpty()) {
                dictionary.addWords(customWords);
            }
            if (!stopwords.isEmpty()) {
                dictionary.disableWords(stopwords);
            }
            
        } catch (IOException e) {
            logger.error("加载自定义词典失败", e);
        }
    }
    
    /**
     * 添加自定义词条
     */
    public void addCustomWord(String word) throws IOException {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        Path customDictPath = Paths.get(dictionaryPath, "custom.dic");
        Files.write(
            customDictPath,
            (word.trim() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
            java.nio.file.StandardOpenOption.APPEND,
            java.nio.file.StandardOpenOption.CREATE
        );
        
        // 重新加载词典
        List<String> words = new ArrayList<>();
        words.add(word.trim());
        dictionary.addWords(words);
        logger.info("添加自定义词条: {}", word);
    }
    
    /**
     * 添加停用词
     */
    public void addStopword(String word) throws IOException {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        Path stopwordsDictPath = Paths.get(dictionaryPath, "stopwords.dic");
        Files.write(
            stopwordsDictPath,
            (word.trim() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
            java.nio.file.StandardOpenOption.APPEND,
            java.nio.file.StandardOpenOption.CREATE
        );
        
        // 重新加载词典
        List<String> words = new ArrayList<>();
        words.add(word.trim());
        dictionary.disableWords(words);
        logger.info("添加停用词: {}", word);
    }
    
    /**
     * 读取词典文件
     */
    private List<String> readDictionaryFile(Path path) throws IOException {
        List<String> words = new ArrayList<>();
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        words.add(line);
                    }
                }
            }
        }
        return words;
    }
} 