package org.example.web2_7.service;

import org.example.web2_7.pojo.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        "综合新闻", "体育竞技", "娱乐休闲", "金融财经", "科技前沿", 
        "生活消费", "文化艺术", "旅游出行", "房产家居", "教育学习", 
        "地域新闻", "医疗健康"
    };
    
    // 分类关键词映射，用于基于内容识别标签
    private static final Map<String, List<String>> TAG_KEYWORDS = new HashMap<>();
    
    // TF-IDF算法相关参数
    private static final double MATCH_THRESHOLD = 0.005; // 降低标签匹配阈值，使更多文章有标签
    private static final int MAX_TAGS_PER_ARTICLE = 5; // 每篇文章最多标签数
    
    // 停用词列表
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "和", "与", "是", "在", "我", "有", "你", "他", "她", "它", "这", "那", "都", "会", "对", "到",
        "说", "等", "很", "啊", "吧", "呢", "吗", "要", "看", "来", "去", "做", "可以", "自己", "没有", "如果"
    ));

    // 新增：标签文章缓存，用于存储每个标签筛选后的文章列表
    private static final Map<String, List<Article>> TAG_ARTICLES_CACHE = new ConcurrentHashMap<>();
    
    // 新增：文章标签缓存，用于存储每篇文章的标签列表
    private static final Map<Integer, List<String>> ARTICLE_TAGS_CACHE = new ConcurrentHashMap<>();

    static {
        // 初始化各标签的关键词，基于新的分类体系
        TAG_KEYWORDS.put("综合新闻", Arrays.asList("新闻", "时事", "特朗普", "美国", "中国", "突发", "冲突", "资讯", "播报", "快讯", "要闻", "实时", "焦点", "动态", "简讯", "一带一路","哪吒2"));
        
        TAG_KEYWORDS.put("体育竞技", Arrays.asList(
            // 原有关键词
            "体育", "NBA", "篮球", "足球", "排球", "乒乓球", "羽毛球", "网球", "世界杯", "排位赛", "比赛赛事", "F1", "运动员", "球星", "体坛", "体育赛事", "奥运", "亚运",
            // 羽毛球相关新增关键词
            "苏迪曼杯", "尤伯杯", "汤姆斯杯", "羽联", "羽总", "全英赛", "马来西亚公开赛", "印尼公开赛", "丹麦公开赛", "日本公开赛", 
            "中国羽毛球公开赛", "羽毛球大师赛", "世锦赛", "羽联世锦赛", "羽毛球世锦赛", "羽毛球团体赛", "羽毛球混合团体赛",
            "决赛", "半决赛", "四分之一决赛", "八强", "四强", "冠军", "亚军", "季军", "夺冠", "晋级", "国羽",
            "石宇奇", "陈雨菲", "何冰娇", "谌龙", "黄宇翔", "安赛龙", "桃田贤斗", "戴资颖", "奥原希望", "马林", "因达农",
            // F1赛车相关新增关键词
            "赛车", "方程式", "迈阿密大奖赛", "摩纳哥大奖赛", "蒙特卡洛大奖赛", "上海大奖赛", "墨尔本大奖赛", 
            "练习赛", "冲刺赛", "杆位", "赛道", "维斯塔潘", "汉密尔顿", "勒克莱尔", "法拉利", "红牛车队", "梅赛德斯", 
            "迈凯伦", "阿斯顿马丁", "威廉姆斯", "阿尔法·罗密欧", "轮胎", "进站", "维修区", "积分榜", "车手", "车队",
            "赛事", "大奖赛", "一级方程式", "一级赛车", "围场", "安全车", "DRS", "引擎", "动力单元", "赛季", "巡回赛"
        ));
        
        TAG_KEYWORDS.put("娱乐休闲", Arrays.asList("娱乐", "音乐", "游戏", "明星", "电影", "电视剧", "综艺", "八卦", "演唱会", "歌手", "演员", "导演", "娱乐圈", "电竞", "网游", "手游", "游戏直播", "直播", "主播", "短视频", "网红","王者荣耀","原神","绝区零","明日方舟","第五人格"));
        
        TAG_KEYWORDS.put("金融财经", Arrays.asList("财经", "股票", "基金", "证券", "理财", "投资", "金融", "经济", "A股", "港股", "美股", "债券", "期货", "外汇", "虚拟货币", "比特币", "区块链", "银行", "保险", "通货膨胀", "政策", "宏观", "黄金"));
        
        TAG_KEYWORDS.put("科技前沿", Arrays.asList("科技", "手机", "数码", "MCP", "AI", "大模型", "互联网", "数字化", "云计算", "大数据", "芯片", "软件", "硬件", "程序", "编程", "算法", "智能家居", "智能驾驶", "VR", "AR", "元宇宙", "机器人", "5G", "6G"));
        
        TAG_KEYWORDS.put("生活消费", Arrays.asList("汽车", "家电", "时尚", "美食", "服装", "购物", "消费", "品牌", "电器", "数码", "家用", "厨房", "电子产品", "新车", "二手车", "电动车", "汽车保养", "家电维修", "时装", "奢侈品", "潮牌", "搭配"));
        
        TAG_KEYWORDS.put("文化艺术", Arrays.asList("文化", "艺术", "历史", "传统", "博物馆", "展览", "文学", "诗歌", "小说", "散文", "戏曲", "电影艺术", "音乐艺术", "绘画", "雕塑", "摄影", "建筑", "设计", "文创", "非遗", "传承", "文物", "古籍", "故宫", "文物"));
        
        TAG_KEYWORDS.put("旅游出行", Arrays.asList("旅游", "旅行", "景点", "出游", "度假", "酒店", "民宿", "航班", "高铁", "火车", "公路", "自驾", "徒步", "户外", "探险", "风景", "名胜古迹", "网红打卡点", "自然景观", "特色小镇", "乡村旅游", "文化旅游"));
        
        TAG_KEYWORDS.put("房产家居", Arrays.asList("房产", "家居", "楼市", "房价", "地产", "住宅", "商铺", "写字楼", "公寓", "别墅", "二手房", "租赁", "装修", "家具", "软装", "硬装", "卧室", "客厅", "厨房", "卫浴", "房贷", "物业", "小区"));
        
        TAG_KEYWORDS.put("教育学习", Arrays.asList(
            // 原有关键词
            "教育", "学习", "学校", "大学", "高中", "初中", "小学", "幼儿园", "考试", "高考", "中考", "考研", "留学", 
            "培训", "学科", "课程", "教材", "师资", "校园", "在线教育", "教学", "学历", "职业教育",
            // 新增高等教育相关关键词
            "研究生", "本科", "博士", "硕士", "学士", "学位", "毕业", "毕业论文", "毕业设计", "论文答辩", 
            "保研", "考公", "考编", "教师资格证", "考证", "考试", "英语四级", "英语六级", "四六级", "考试成绩", 
            "院校", "专业", "录取", "录取分数线", "高校", "名校", "985", "211", "双一流", "重点大学", 
            "北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学", "南京大学", "武汉大学", "中国人民大学",
            "哈尔滨工业大学", "西安交通大学", "北京信息科技大学", "北信科", "首都经济贸易大学", "首经贸", 
            "北京工业大学", "北工大", "北京科技大学", "北科大", "北京师范大学", "北师大",
            "自考", "成人高考", "专升本", "专科", "大专", "读博", "博士后", "博导", "导师", "指导教师",
            "学生会", "社团", "实习", "实验室", "创新创业", "竞赛", "学术", "论文", "期刊", "学报",
            "教授", "副教授", "讲师", "助教", "辅导员", "班主任", "班级", "大一", "大二", "大三", "大四",
            "宿舍", "寝室", "食堂", "奖学金", "助学金", "贫困生", "学费", "课外活动", "课堂", "课程设计",
            "考研辅导", "考研资料", "考研真题", "考研政治", "考研英语", "考研数学", "考研专业课",
            "全日制", "非全日制", "在职研究生", "同等学力", "远程教育", "网络教育", "继续教育", "终身学习"
        ));
        
        TAG_KEYWORDS.put("地域新闻", Arrays.asList("北京", "上海", "广州", "深圳", "天津", "重庆", "南京", "杭州", "武汉", "成都", "西安", "长沙", "郑州", "青岛", "大连", "沈阳", "哈尔滨", "南宁", "省市", "城市", "区域", "本地", "吉林", "黑龙江", "东北", "白山", "松花江"));
        
        TAG_KEYWORDS.put("医疗健康", Arrays.asList("健康", "医疗", "医院", "医生", "药品", "疾病", "患者", "治疗", "手术", "保健", "养生", "预防", "康复", "心理", "脱发", "减肥", "锻炼", "健身", "体检", "营养", "饮食", "中医", "西医", "疫苗"));
    }

    /**
     * 获取所有可用标签
     */
    public List<String> getAllTags() {
        return Arrays.asList(PREDEFINED_TAGS);
    }
    
    /**
     * 基于优化后的TF-IDF算法提取文章标签
     * 增加了缓存支持，避免重复计算
     */
    public List<String> extractTags(Article article) {
        if (article == null || article.getTitle() == null) {
            return Collections.emptyList();
        }
        
        // 首先检查缓存中是否已存在该文章的标签
        if (ARTICLE_TAGS_CACHE.containsKey(article.getId())) {
            logger.debug("从缓存获取文章ID={}的标签", article.getId());
            return ARTICLE_TAGS_CACHE.get(article.getId());
        }
        
        // 合并标题和内容，标题权重更高（重复5次）
        String title = article.getTitle().toLowerCase();
        String titleWeighted = title + " " + title + " " + title + " " + title + " " + title; // 增加标题权重
        String content = article.getContent() != null ? article.getContent().toLowerCase() : "";
        String fullText = titleWeighted + " " + content;
        
        // 提取文章关键词
        List<String> articleKeywords = article.getKeywords() != null 
            ? Arrays.asList(article.getKeywords().split(",")) 
            : new ArrayList<>();
            
        // 分词并计算词频
        Map<String, Integer> wordFrequency = calculateWordFrequency(fullText);
        
        // 计算每个标签的TF-IDF分数
        Map<String, Double> tagScores = new HashMap<>();
        Map<String, List<String>> matchedKeywords = new HashMap<>();
        
        for (String tag : PREDEFINED_TAGS) {
            List<String> keywords = TAG_KEYWORDS.get(tag);
            if (keywords == null) continue;
            
            double tagScore = 0.0;
            List<String> matches = new ArrayList<>();
            
            for (String keyword : keywords) {
                String keywordLower = keyword.toLowerCase();
                double keywordScore = 0.0;
                
                // 1. 检查文章词频中是否有关键词
                if (wordFrequency.containsKey(keywordLower)) {
                    // 改进TF-IDF计算方式
                    double tf = (double) wordFrequency.get(keywordLower) / Math.max(50, wordFrequency.size()); // 标准化词频
                    double idf = Math.log10(2 + keywordLower.length()); // 更合理的IDF计算
                    double tfidf = tf * idf * 10; // 放大TF-IDF分数，避免过小
                    
                    keywordScore += tfidf;
                    
                    // 2. 检查标题是否直接包含关键词，提高权重
                    if (title.contains(keywordLower)) {
                        keywordScore += 0.8; // 标题匹配加权
                        
                        // 3. 关键词在标题开头，进一步提高权重
                        if (title.startsWith(keywordLower)) {
                            keywordScore += 0.5;
                        }
                    }
                    
                    matches.add(keyword);
                }
                
                // 4. 检查是否与文章关键词匹配
                for (String articleKeyword : articleKeywords) {
                    if (articleKeyword.toLowerCase().contains(keywordLower) || 
                        keywordLower.contains(articleKeyword.toLowerCase())) {
                        keywordScore += 0.7; // 关键词匹配加权
                        if (!matches.contains(keyword)) {
                            matches.add(keyword);
                        }
                        break;
                    }
                }
                
                tagScore += keywordScore;
            }
            
            // 存储标签得分和匹配的关键词
            if (tagScore > 0 || !matches.isEmpty()) {
                tagScores.put(tag, tagScore);
                matchedKeywords.put(tag, matches);
            }
        }
        
        // 按分数排序并选择得分最高的标签
        List<Map.Entry<String, Double>> sortedTags = new ArrayList<>(tagScores.entrySet());
        sortedTags.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        List<String> selectedTags = new ArrayList<>();
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("文章「").append(article.getTitle()).append("」的标签匹配详情:\n");
        
        for (Map.Entry<String, Double> entry : sortedTags) {
            String tag = entry.getKey();
            double score = entry.getValue();
            
            // 只选择得分超过阈值的标签，且最多选择MAX_TAGS_PER_ARTICLE个
            if (score >= MATCH_THRESHOLD && selectedTags.size() < MAX_TAGS_PER_ARTICLE) {
                selectedTags.add(tag);
                
                // 记录标签匹配详情
                logMessage.append("- 标签「").append(tag).append("」(得分: ").append(String.format("%.4f", score))
                         .append(")，匹配关键词: ").append(matchedKeywords.get(tag)).append("\n");
            }
        }
        
        // 如果没有匹配到任何标签，使用最高分标签或基于文章名智能匹配
        if (selectedTags.isEmpty()) {
            // 尝试从文章标题直接推断标签
            String inferredTag = inferTagFromTitle(article.getTitle());
            if (inferredTag != null) {
                selectedTags.add(inferredTag);
                logMessage.append("- 根据标题直接推断标签「").append(inferredTag).append("」\n");
            }
            // 如果还是为空，取最高分标签（如果有）
            else if (!sortedTags.isEmpty()) {
                String topTag = sortedTags.get(0).getKey();
                selectedTags.add(topTag);
                logMessage.append("- 未达到阈值，但选择得分最高的标签「").append(topTag).append("」(得分: ")
                         .append(String.format("%.4f", sortedTags.get(0).getValue()))
                         .append(")，匹配关键词: ").append(matchedKeywords.get(topTag)).append("\n");
            }
            // 最后兜底：如果仍然没有标签，选择"综合新闻"
            else {
                selectedTags.add("综合新闻");
                logMessage.append("- 无法匹配任何标签，默认选择「综合新闻」标签\n");
            }
        }
        
        logger.info(logMessage.toString());
        logger.info("文章「{}」最终选择的标签: {}", article.getTitle(), selectedTags);
        
        // 将结果保存到缓存中
        if (article.getId() != null) {
            ARTICLE_TAGS_CACHE.put(article.getId(), selectedTags);
            
            // 更新标签-文章映射缓存
            for (String tag : selectedTags) {
                TAG_ARTICLES_CACHE.computeIfAbsent(tag, k -> new ArrayList<>()).add(article);
            }
        }
        
        return selectedTags;
    }
    
    /**
     * 从标题直接推断可能的标签
     */
    private String inferTagFromTitle(String title) {
        if (title == null) return null;
        
        String lowerTitle = title.toLowerCase();
        
        // 使用一些常见的直接匹配规则
        // 增强体育竞技标签匹配能力
        if (lowerTitle.contains("体育") || lowerTitle.contains("球赛") || 
            lowerTitle.contains("nba") || lowerTitle.contains("足球") ||
            lowerTitle.contains("篮球") || lowerTitle.contains("排球") ||
            lowerTitle.contains("乒乓球") || lowerTitle.contains("羽毛球") ||
            lowerTitle.contains("苏迪曼") || lowerTitle.contains("尤伯杯") ||
            lowerTitle.contains("汤姆斯杯") || lowerTitle.contains("羽联") ||
            lowerTitle.contains("世锦赛") || lowerTitle.contains("奥运") ||
            lowerTitle.contains("亚运") || lowerTitle.contains("晋级") ||
            lowerTitle.contains("决赛") || lowerTitle.contains("半决赛") ||
            lowerTitle.contains("冠军") || lowerTitle.contains("亚军") ||
            lowerTitle.contains("夺冠") || lowerTitle.contains("国羽") ||
            lowerTitle.contains("F1") || lowerTitle.contains("世界杯") ||
            lowerTitle.contains("全英赛") || lowerTitle.contains("公开赛") ||
            // 增强对F1和赛车内容的识别
            lowerTitle.contains("赛车") || lowerTitle.contains("方程式") || 
            lowerTitle.contains("练习赛") || lowerTitle.contains("冲刺赛") || 
            lowerTitle.contains("排位赛") || lowerTitle.contains("杆位") || 
            lowerTitle.contains("维斯塔潘") || lowerTitle.contains("汉密尔顿") || 
            lowerTitle.contains("法拉利") || lowerTitle.contains("红牛车队") || 
            lowerTitle.contains("梅赛德斯") || lowerTitle.contains("迈凯伦") || 
            lowerTitle.contains("大奖赛") || 
            (lowerTitle.contains("比赛") && (lowerTitle.contains("胜") || lowerTitle.contains("赢") || lowerTitle.contains("输")))) {
            return "体育竞技";
        }
        
        if (lowerTitle.contains("游戏") || lowerTitle.contains("电影") || 
        
            lowerTitle.contains("娱乐") || lowerTitle.contains("明星") ||
            lowerTitle.contains("音乐") || lowerTitle.contains("演唱会")) {
            return "娱乐休闲";
        }
        if (lowerTitle.contains("楼市") || lowerTitle.contains("房价") ||
            lowerTitle.contains("房产") || lowerTitle.contains("装修") ||
            lowerTitle.contains("楼盘") || lowerTitle.contains("恒大")) {
            return "房产家居";
        }
        
        if (lowerTitle.contains("股票") || lowerTitle.contains("基金") || 
            lowerTitle.contains("财经") || lowerTitle.contains("经济") ||
            lowerTitle.contains("金融") || lowerTitle.contains("理财")) {
            return "金融财经";
        }
        
        // 增强教育学习标签的匹配能力
        if (lowerTitle.contains("教育") || lowerTitle.contains("学习") || 
            lowerTitle.contains("大学") || lowerTitle.contains("高校") || 
            lowerTitle.contains("专业") || lowerTitle.contains("本科") || 
            lowerTitle.contains("学院") || lowerTitle.contains("学校") ||
            lowerTitle.contains("研究生") || lowerTitle.contains("博士") ||
            lowerTitle.contains("硕士") || lowerTitle.contains("课程") ||
            lowerTitle.contains("考试") || lowerTitle.contains("学历") ||
            lowerTitle.contains("高考") || lowerTitle.contains("考研")) {
            return "教育学习";
        }
        
        if (lowerTitle.contains("科技") || lowerTitle.contains("手机") || 
            lowerTitle.contains("数码") || lowerTitle.contains("ai") ||
            lowerTitle.contains("人工智能") || lowerTitle.contains("芯片")) {
            return "科技前沿";
        }
        
        if (lowerTitle.contains("故宫") || lowerTitle.contains("展览") || 
            lowerTitle.contains("文化") || lowerTitle.contains("艺术") ||
            lowerTitle.contains("历史") || lowerTitle.contains("博物馆")) {
            return "文化艺术";
        }
        
        if (lowerTitle.contains("旅游") || lowerTitle.contains("景点") || 
            lowerTitle.contains("度假") || lowerTitle.contains("酒店") ||
            lowerTitle.contains("旅行")) {
            return "旅游出行";
        }
        
        if (lowerTitle.contains("北京") || lowerTitle.contains("上海") || 
            lowerTitle.contains("广州") || lowerTitle.contains("深圳") ||
            lowerTitle.contains("吉林") || lowerTitle.contains("东北") ||
            lowerTitle.contains("西安") || lowerTitle.contains("长沙")) {
            return "地域新闻";
        }
        
        return null;
    }
    
    /**
     * 计算文本中词语的频率
     */
    private Map<String, Integer> calculateWordFrequency(String text) {
        Map<String, Integer> wordFreq = new HashMap<>();
        
        if (text == null || text.isEmpty()) {
            return wordFreq;
        }
        
        // 改进分词（简单实现，按空格和常见标点符号分割）
        String[] words = text.split("\\s+|[,.。，、；:：\"'\\(\\)（）\\[\\]【】《》?？!！]+");
        
        for (String word : words) {
            word = word.trim().toLowerCase();
            // 过滤空词和停用词，允许长度>=2的词
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }
        
        return wordFreq;
    }
    
    /**
     * 清除缓存
     * 当有新文章添加或现有文章更新时调用此方法
     */
    public void clearCache() {
        logger.info("清除标签缓存");
        TAG_ARTICLES_CACHE.clear();
        ARTICLE_TAGS_CACHE.clear();
    }
    
    /**
     * 根据标签筛选文章
     * 修改后的方法，增加了缓存支持
     */
    public List<Article> filterArticlesByTag(List<Article> articles, String tag) {
        if (tag == null || tag.isEmpty() || "全部".equals(tag)) {
            return articles;
        }
        
        logger.info("开始根据标签「{}」筛选文章，共{}篇待筛选", tag, articles.size());
        
        // 先检查缓存中是否有结果
        if (TAG_ARTICLES_CACHE.containsKey(tag)) {
            List<Article> cachedArticles = TAG_ARTICLES_CACHE.get(tag);
            
            // 验证缓存的文章是否都在当前文章列表中
            // 只返回同时存在于缓存和当前文章列表中的文章
            List<Article> validCachedArticles = new ArrayList<>();
            Set<Integer> currentArticleIds = new HashSet<>();
            for (Article article : articles) {
                if (article.getId() != null) {
                    currentArticleIds.add(article.getId());
                }
            }
            
            for (Article cachedArticle : cachedArticles) {
                if (cachedArticle.getId() != null && currentArticleIds.contains(cachedArticle.getId())) {
                    validCachedArticles.add(cachedArticle);
                }
            }
            
            if (!validCachedArticles.isEmpty()) {
                logger.info("从缓存获取标签「{}」筛选结果: {}篇文章", tag, validCachedArticles.size());
                return validCachedArticles;
            } else {
                logger.info("缓存中的文章已过期，重新进行筛选");
            }
        }
        
        // 缓存中没有结果，或缓存结果不完整，重新筛选
        List<Article> filteredArticles = new ArrayList<>();
        for (Article article : articles) {
            List<String> articleTags = extractTags(article);
            if (articleTags.contains(tag)) {
                filteredArticles.add(article);
            }
        }
        
        // 更新缓存
        TAG_ARTICLES_CACHE.put(tag, new ArrayList<>(filteredArticles));
        
        logger.info("标签「{}」筛选结果: {}篇文章", tag, filteredArticles.size());
        return filteredArticles;
    }
} 