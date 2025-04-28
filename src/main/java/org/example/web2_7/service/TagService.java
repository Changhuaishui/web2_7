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
        "前沿科技", "人工智能", "金融财经", "教育学习", "医疗健康", "文化艺术", 
        "娱乐八卦", "体育竞技", "政治时事", "历史研究", "旅游出行", "美食烹饪"
    };
    
    // 分类关键词映射，用于基于内容识别标签
    private static final Map<String, List<String>> TAG_KEYWORDS = new HashMap<>();

    static {
        // 初始化各标签的关键词，优化
        TAG_KEYWORDS.put("前沿科技", Arrays.asList("科技", "技术", "数字", "互联网", "物联网", "区块链", "云计算", "大数据", "硬件", "软件", "产品", "科技成果", "科研突破", "元宇宙", "Web3.0", "量子计算", "6G技术"));
        TAG_KEYWORDS.put("人工智能", Arrays.asList("人工智能", "AI", "机器学习", "深度学习", "神经网络", "算法", "大模型", "ChatGPT", "智能机器人", "自然语言处理", "计算机视觉", "AIGC", "文心一言", "通义千问", "星链智擎"));
        TAG_KEYWORDS.put("金融财经", Arrays.asList("金融", "经济", "宏观经济", "微观经济", "投资", "股票", "理财", "银行", "保险", "基金", "债券", "金融市场", "金融政策", "数字人民币", "绿色金融", "金融科技监管"));
        TAG_KEYWORDS.put("教育学习", Arrays.asList("教育", "学习", "培训", "学校", "幼儿园", "小学", "中学", "大学", "课程", "教学", "知识", "考试", "教育改革", "学术研究", "在线教育", "职业教育", "双减政策", "ChatGPT教育应用"));
        TAG_KEYWORDS.put("医疗健康", Arrays.asList("医疗", "健康", "医院", "诊所", "疾病", "病症", "药品", "治疗", "保健", "医生", "护士", "医学研究", "医疗技术", "基因编辑", "mRNA疫苗", "远程医疗", "心理健康"));
        TAG_KEYWORDS.put("文化艺术", Arrays.asList("文化", "艺术", "历史文化", "现代文化", "传统艺术", "当代艺术", "历史", "传统", "文学", "音乐", "绘画", "设计", "雕塑", "建筑艺术", "文化遗产", "国潮文化", "数字藏品", "非遗传承"));
        TAG_KEYWORDS.put("娱乐八卦", Arrays.asList("娱乐", "明星", "电影", "电视", "电视剧", "网剧", "游戏", "综艺", "音乐", "演唱会", "娱乐活动", "明星绯闻", "电竞亚运会", "短剧", "AI换脸影视", "虚拟现实娱乐"));
        TAG_KEYWORDS.put("体育竞技", Arrays.asList("体育", "运动", "足球", "篮球", "排球", "健身", "比赛", "奥运", "联赛", "体育赛事", "运动员", "体育精神", "杭州亚运会", "巴黎奥运会筹备", "电竞体育", "极限运动"));
        TAG_KEYWORDS.put("政治时事", Arrays.asList("政治", "政府", "政策", "法律", "国际政治", "国内政治", "外交", "选举", "时事热点", "政治动态", "俄乌冲突新进展", "巴以冲突", "中美关系动态", "一带一路倡议新成果"));
        TAG_KEYWORDS.put("历史研究", Arrays.asList("历史", "古代史", "近代史", "现代史", "战争", "人物", "王朝", "考古", "文明", "遗址", "历史事件", "历史人物研究", "三星堆新发现", "海昏侯墓研究进展", "历史文化数字化"));
        TAG_KEYWORDS.put("旅游出行", Arrays.asList("旅游", "旅行", "景点", "名胜古迹", "自然景观", "酒店", "度假", "自驾", "攻略", "旅游目的地", "旅游文化", "冰雪旅游", "红色旅游", "乡村旅游", "旅游消费券"));
        TAG_KEYWORDS.put("美食烹饪", Arrays.asList("美食", "餐饮", "菜品", "中餐", "西餐", "厨艺", "食材", "烹饪", "味道", "小吃", "美食节", "烹饪技巧", "预制菜", "健康饮食", "分子美食", "地方特色美食挖掘"));
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