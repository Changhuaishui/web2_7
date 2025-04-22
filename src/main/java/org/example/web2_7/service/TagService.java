package org.example.web2_7.service;

import org.example.web2_7.pojo.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文章标签服务
 * 基于TF-IDF算法提取文章关键词作为标签
 */
@Service
public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    
    // 预定义的标签分类
    private static final String[] PREDEFINED_TAGS = {
        "科技", "AI", "金融", "教育", "医疗", "文化", 
        "娱乐", "体育", "政治", "历史", "旅游", "美食"
    };
    
    // 分类关键词映射，用于基于内容识别标签
    private static final Map<String, List<String>> TAG_KEYWORDS = new HashMap<>();
    
    static {
        // 初始化各标签的关键词
        TAG_KEYWORDS.put("科技", Arrays.asList("科技", "技术", "数字", "互联网", "硬件", "软件", "产品"));
        TAG_KEYWORDS.put("AI", Arrays.asList("人工智能", "AI", "机器学习", "深度学习", "神经网络", "算法", "大模型", "ChatGPT"));
        TAG_KEYWORDS.put("金融", Arrays.asList("金融", "经济", "投资", "股票", "理财", "银行", "保险", "基金"));
        TAG_KEYWORDS.put("教育", Arrays.asList("教育", "学习", "培训", "学校", "课程", "教学", "知识", "考试"));
        TAG_KEYWORDS.put("医疗", Arrays.asList("医疗", "健康", "医院", "疾病", "药品", "治疗", "保健", "医生"));
        TAG_KEYWORDS.put("文化", Arrays.asList("文化", "艺术", "历史", "传统", "文学", "音乐", "绘画", "设计"));
        TAG_KEYWORDS.put("娱乐", Arrays.asList("娱乐", "明星", "电影", "电视", "游戏", "综艺", "音乐", "演唱会"));
        TAG_KEYWORDS.put("体育", Arrays.asList("体育", "运动", "足球", "篮球", "健身", "比赛", "奥运", "联赛"));
        TAG_KEYWORDS.put("政治", Arrays.asList("政治", "政府", "政策", "法律", "国际", "外交", "选举", "时事"));
        TAG_KEYWORDS.put("历史", Arrays.asList("历史", "古代", "战争", "人物", "王朝", "考古", "文明", "遗址"));
        TAG_KEYWORDS.put("旅游", Arrays.asList("旅游", "旅行", "景点", "酒店", "度假", "自驾", "攻略", "文化"));
        TAG_KEYWORDS.put("美食", Arrays.asList("美食", "餐饮", "菜品", "厨艺", "食材", "烹饪", "味道", "小吃"));
    }
    
    /**
     * 获取所有可用标签
     */
    public List<String> getAllTags() {
        return Arrays.asList(PREDEFINED_TAGS);
    }
    
    /**
     * 基于文章内容提取标签
     * 简单实现：检查标题和内容中是否包含标签关键词
     */
    public List<String> extractTags(Article article) {
        if (article == null || article.getTitle() == null) {
            return Collections.emptyList();
        }
        
        Set<String> tags = new HashSet<>();
        String title = article.getTitle().toLowerCase();
        String content = article.getContent() != null ? article.getContent().toLowerCase() : "";
        
        // 遍历所有预定义标签
        for (String tag : PREDEFINED_TAGS) {
            List<String> keywords = TAG_KEYWORDS.get(tag);
            if (keywords != null) {
                // 检查标题和内容中是否包含该标签的关键词
                for (String keyword : keywords) {
                    if (title.contains(keyword.toLowerCase()) || 
                        content.contains(keyword.toLowerCase())) {
                        tags.add(tag);
                        break;
                    }
                }
            }
        }
        
        // 如果没有识别出标签，添加一个默认标签
        if (tags.isEmpty()) {
            tags.add("其他");
        }
        
        List<String> result = new ArrayList<>(tags);
        logger.info("为文章「{}」提取到标签: {}", article.getTitle(), result);
        return result;
    }
    
    /**
     * 根据标签筛选文章
     */
    public List<Article> filterArticlesByTag(List<Article> articles, String tag) {
        if (tag == null || tag.isEmpty() || "全部".equals(tag)) {
            return articles;
        }
        
        List<Article> filteredArticles = new ArrayList<>();
        for (Article article : articles) {
            List<String> articleTags = extractTags(article);
            if (articleTags.contains(tag)) {
                filteredArticles.add(article);
            }
        }
        
        logger.info("标签「{}」筛选结果: {}篇文章", tag, filteredArticles.size());
        return filteredArticles;
    }
} 