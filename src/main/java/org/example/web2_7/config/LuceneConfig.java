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
                
                // 创建一个默认的自定义词典文件
                Path mainDict = dictPath.resolve("custom_main.dic");
                if (!Files.exists(mainDict)) {
                    List<String> defaultWords = List.of(
                        "白山", "黑水", "长白山", "吉林省",
                        "ChatGPT", "AIGC", "人工智能", 
                        "大模型", "机器学习"
                    );
                    Files.write(mainDict, defaultWords);
                    System.out.println("已创建默认自定义词典文件: " + mainDict);
                }
                
                // 创建一个默认的停用词典文件
                Path stopwordDict = dictPath.resolve("custom_stopword.dic");
                if (!Files.exists(stopwordDict)) {
                    List<String> defaultStopwords = List.of(
                        "的", "了", "是", "在", "我", "有", "和", "就", 
                        "不", "人", "都", "一", "一个", "上", "也", "很", 
                        "到", "说", "要", "去", "你", "会", "着", "没有", 
                        "看", "好", "自己", "这"
                    );
                    Files.write(stopwordDict, defaultStopwords);
                    System.out.println("已创建默认停用词典文件: " + stopwordDict);
                }
            }
            
            // 设置自定义词典路径
            System.setProperty("dic.path", dictPath.toAbsolutePath().toString());
            System.out.println("自定义词典路径: " + dictPath.toAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("初始化自定义词典失败: " + e.getMessage());
            e.printStackTrace();
        }
        
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