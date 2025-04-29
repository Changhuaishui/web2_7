package org.example.web2_7.config;

import org.apache.lucene.analysis.Analyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.annotation.PostConstruct;

@Configuration
public class LuceneConfig {
    
    @Value("${lucene.index.path:lucene_index}")
    private String indexPath;
    
    @Value("${ik.analyzer.useSmart:true}")
    private boolean useSmart;
    
    @Value("${ik.analyzer.dictionary.path:custom_dictionary}")
    private String dictionaryPath;
    
    @PostConstruct
    public void init() {
        // 设置是否使用智能分词
        System.setProperty("use_smart", String.valueOf(useSmart));
        // 初始化词典
        org.wltea.analyzer.cfg.Configuration cfg = DefaultConfig.getInstance();
        cfg.setUseSmart(useSmart);
        Dictionary.initial(cfg);
    }
    
    /**
     * 配置IK分词器
     */
    @Bean
    public Analyzer ikAnalyzer() {
        return new IKAnalyzer(useSmart);
    }
    
    /**
     * 确保索引目录存在
     */
    @Bean
    public String indexDirectory() throws IOException {
        Path path = Paths.get(indexPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return indexPath;
    }
    
    /**
     * 初始化IK分词器词典
     */
    @Bean
    public Dictionary ikDictionary() {
        return Dictionary.getSingleton();
    }
    
    /**
     * 获取自定义词典路径
     */
    @Bean
    public String customDictionaryPath() throws IOException {
        Path path = Paths.get(dictionaryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            
            // 创建自定义词典文件
            Path customDict = path.resolve("custom.dic");
            if (!Files.exists(customDict)) {
                Files.createFile(customDict);
            }
            
            // 创建停用词词典文件
            Path stopwordsDict = path.resolve("stopwords.dic");
            if (!Files.exists(stopwordsDict)) {
                Files.createFile(stopwordsDict);
            }
        }
        return dictionaryPath;
    }
} 