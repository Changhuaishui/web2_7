package org.example.web2_7.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**为后来准备，暂时用不上
 * TF-IDF计算工具类
 * 用于计算文本关键词权重，实现更精准的搜索和排序
 */
public class TFIDFCalculator {
    private final Analyzer analyzer;
    private final Map<String, Integer> documentFrequencies; // 词在多少文档中出现
    private final int totalDocuments; // 总文档数

    /**
     * 构造函数
     * 
     * @param documents 所有文档的内容列表
     */
    public TFIDFCalculator(List<String> documents) {
        this.analyzer = new IKAnalyzer(true); // 使用IK分词器，开启智能分词
        this.documentFrequencies = new HashMap<>();
        this.totalDocuments = documents.size();
        
        // 计算文档频率
        calculateDocumentFrequencies(documents);
    }

    /**
     * 计算所有文档的词频
     */
    private void calculateDocumentFrequencies(List<String> documents) {
        for (String document : documents) {
            // 获取文档中的唯一词项
            Set<String> uniqueTerms = getUniqueTerms(document);
            
            // 更新文档频率
            for (String term : uniqueTerms) {
                documentFrequencies.put(term, documentFrequencies.getOrDefault(term, 0) + 1);
            }
        }
    }

    /**
     * 获取文本中的唯一词项
     */
    private Set<String> getUniqueTerms(String text) {
        Set<String> terms = new HashSet<>();
        try {
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            while (tokenStream.incrementToken()) {
                terms.add(termAttr.toString());
            }
            
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return terms;
    }

    /**
     * 计算词频 (TF)
     * 词在文档中出现的次数 / 文档总词数
     */
    public Map<String, Double> calculateTF(String document) {
        Map<String, Integer> termFrequency = new HashMap<>();
        Map<String, Double> tf = new HashMap<>();
        int totalTerms = 0;
        
        try {
            TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(document));
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            while (tokenStream.incrementToken()) {
                String term = termAttr.toString();
                termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
                totalTerms++;
            }
            
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 计算每个词的TF值
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / totalTerms);
        }
        
        return tf;
    }

    /**
     * 计算逆文档频率 (IDF)
     * log(总文档数 / (包含该词的文档数 + 1))
     */
    public double calculateIDF(String term) {
        return Math.log((double) totalDocuments / (documentFrequencies.getOrDefault(term, 0) + 1));
    }

    /**
     * 计算TF-IDF值
     * TF * IDF
     */
    public Map<String, Double> calculateTFIDF(String document) {
        Map<String, Double> tf = calculateTF(document);
        Map<String, Double> tfidf = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double tfValue = entry.getValue();
            double idfValue = calculateIDF(term);
            tfidf.put(term, tfValue * idfValue);
        }
        
        return tfidf;
    }
    
    /**
     * 计算查询与文档的相关度分数
     * 基于查询词的TF-IDF值与文档中对应词的TF-IDF值的乘积和
     */
    public double calculateRelevanceScore(String query, String document) {
        Set<String> queryTerms = getUniqueTerms(query);
        Map<String, Double> documentTFIDF = calculateTFIDF(document);
        
        double score = 0.0;
        for (String term : queryTerms) {
            if (documentTFIDF.containsKey(term)) {
                score += documentTFIDF.get(term);
            }
        }
        
        return score;
    }

    /**
     * 根据TF-IDF值提取文档的关键词
     * 返回TF-IDF值最高的topN个词
     */
    public List<Map.Entry<String, Double>> extractKeywords(String document, int topN) {
        Map<String, Double> tfidf = calculateTFIDF(document);
        
        // 将TF-IDF值排序
        List<Map.Entry<String, Double>> sortedTFIDF = new ArrayList<>(tfidf.entrySet());
        sortedTFIDF.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // 返回前topN个关键词
        return sortedTFIDF.subList(0, Math.min(topN, sortedTFIDF.size()));
    }
} 