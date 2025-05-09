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
import java.util.List;

@Configuration
public class LuceneConfig {
    
    @Value("${lucene.index.path:lucene_index}")
    private String indexPath;
    
    @Value("${ik.analyzer.useSmart:false}")
    private boolean useSmart;
    
    @Value("${ik.analyzer.dictionary.path:custom_dictionary}")
    private String dictionaryPath;
    
    @PostConstruct
    public void init() {
        // 设置是否使用智能分词，默认使用细粒度分词以提高召回率
        System.setProperty("use_smart", String.valueOf(useSmart));
        
        try {
            // 确保自定义词典目录存在
            Path dictPath = Paths.get(dictionaryPath);
            if (!Files.exists(dictPath)) {
                Files.createDirectories(dictPath);
            }
            
            // 设置自定义词典路径
            String absolutePath = dictPath.toAbsolutePath().toString();
            System.setProperty("dic.path", absolutePath);
            System.out.println("自定义词典路径: " + absolutePath);
            
            // 检查词库文件
            Path mainDict = dictPath.resolve("custom_main.dic");
            if (Files.exists(mainDict)) {
                System.out.println("词库文件存在: " + mainDict);
                System.out.println("词库文件大小: " + Files.size(mainDict) + " 字节");
            } else {
                System.err.println("词库文件不存在: " + mainDict);
            }
            
            // 重新初始化词典
            Dictionary.initial(DefaultConfig.getInstance());
            System.out.println("词典重新加载完成");
            
        } catch (Exception e) {
            System.err.println("初始化自定义词典失败: " + e.getMessage());
            e.printStackTrace();
        }
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