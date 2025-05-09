package org.example.web2_7.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.utils.LuceneIndexManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleSearchService {
    private final LuceneIndexManager indexManager;
    private final ArticleMapper articleMapper;
    private final Analyzer analyzer;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public ArticleSearchService(LuceneIndexManager indexManager, ArticleMapper articleMapper) {
        this.indexManager = indexManager;
        this.articleMapper = articleMapper;
        this.analyzer = new IKAnalyzer(false);
    }

    @PostConstruct
    public void initIndex() {
        try {
            // 获取所有文章
            List<Article> articles = articleMapper.findAllOrderByPublishTime();
            if (articles != null && !articles.isEmpty()) {
                // 重建索引
                indexManager.createOrUpdateIndex(articles);
                System.out.println("成功初始化 " + articles.size() + " 篇文章的搜索索引");
            } else {
                System.out.println("没有找到文章，跳过索引初始化");
            }
        } catch (Exception e) {
            System.err.println("初始化搜索索引失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Article> searchArticles(String keyword) throws Exception {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 定义搜索字段及其权重
            String[] fields = {"title^2.0", "content", "author", "accountName", "fullText^1.5"};
            
            // 创建多字段查询解析器
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            
            // 设置查询解析器的默认操作为OR，提高召回率
            parser.setDefaultOperator(QueryParser.Operator.OR);
            
            // 处理特殊字符而不是直接转义
            keyword = preprocessKeyword(keyword);
            
            // 解析查询
            Query query = parser.parse(keyword);
            
            // 执行搜索
            IndexReader reader = null;
            try {
                reader = indexManager.getIndexReader();
                IndexSearcher searcher = new IndexSearcher(reader);
                
                // 创建排序器（按发布时间降序）
                Sort sort = new Sort(new SortField("publishTime", SortField.Type.STRING, true));
                
                // 执行查询，获取前100条结果
                TopDocs topDocs = searcher.search(query, 100, sort);
                
                List<Article> results = new ArrayList<>();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    
                    // 将Document转换为Article对象
                    Article article = new Article();
                    article.setId(Integer.parseInt(doc.get("id")));
                    article.setTitle(doc.get("title"));
                    article.setContent(doc.get("content"));
                    article.setAuthor(doc.get("author"));
                    article.setAccountName(doc.get("accountName"));
                    article.setUrl(doc.get("url"));
                    article.setSourceUrl(doc.get("sourceUrl"));
                    
                    // 处理发布时间
                    String publishTimeStr = doc.get("publishTime");
                    if (publishTimeStr != null && !publishTimeStr.isEmpty()) {
                        try {
                            LocalDateTime publishTime = LocalDateTime.parse(publishTimeStr, DATE_FORMATTER);
                            article.setPublishTime(publishTime);
                        } catch (Exception e) {
                            System.err.println("解析发布时间失败: " + publishTimeStr);
                            article.setPublishTime(null);
                        }
                    }
                    
                    article.setIsDeleted(Boolean.parseBoolean(doc.get("isDeleted")));
                    
                    // 只返回未删除的文章
                    if (!article.getIsDeleted()) {
                        results.add(article);
                    }
                }
                
                return results;
            } catch (IOException e) {
                System.err.println("搜索过程中发生错误: " + e.getMessage());
                // 如果索引读取失败，尝试重建索引
                initIndex();
                throw new Exception("搜索服务暂时不可用，请稍后重试");
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception e) {
            System.err.println("搜索失败: " + e.getMessage());
            throw new Exception("搜索失败：" + e.getMessage());
        }
    }

    // 重建所有文章的索引
    public void rebuildIndex(List<Article> articles) throws IOException {
        indexManager.createOrUpdateIndex(articles);
    }

    // 更新单篇文章的索引
    public void updateArticleIndex(Article article) throws IOException {
        IndexReader reader = null;
        try {
            reader = indexManager.getIndexReader();
            // 删除旧的文档
            indexManager.deleteDocument(String.valueOf(article.getId()));
            // 添加新的文档
            List<Article> articles = new ArrayList<>();
            articles.add(article);
            indexManager.createOrUpdateIndex(articles);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 预处理关键词，增强中文搜索的准确性
     * 1. 处理搜索关键词中的特殊字符
     * 2. 处理常见中文标点符号
     * 3. 保留空格以支持词组搜索
     */
    private String preprocessKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        
        // 替换Lucene查询语法中的特殊字符
        String[] specialChars = {"+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\", "/"};
        String result = keyword;
        
        for (String specialChar : specialChars) {
            result = result.replace(specialChar, " ");
        }
        
        // 替换中文标点符号为空格
        result = result.replaceAll("[，。；：！？【】（）]", " ");
        
        // 去除多余空格
        result = result.trim().replaceAll("\\s+", " ");
        
        // 如果处理后为空，返回原关键词的第一个字符
        if (result.isEmpty() && !keyword.isEmpty()) {
            result = String.valueOf(keyword.charAt(0));
        }
        
        return result;
    }
} 