package org.example.web2_7.crawler;
/*
 * @author: chen
 * 爬虫核心实现
 * 数据流向：URL -> 解析页面 -> 提取数据 -> 数据管道 -> 数据库存储
 * -->
 * 输入文章URL后启动爬虫，解析页面内容。
 * -->
 * 提取文章标题、作者、发布时间、正文、图片、原文链接、原作者等。
 * -->
 * 下载图片，并保存到本地。
 * 将解析结果存储到数据库中，并创建搜索索引。
 */
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.select.Selector;
import org.jsoup.nodes.Attribute;
import org.example.web2_7.utils.UlidUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeChatArticleSpider implements PageProcessor {
    // 设置网站信息，为了避免被网站ban，设置User-Agent和Referer等头信息
    private final Site site = Site.me()
            .setRetryTimes(3) // 设置重试次数为3次
            .setSleepTime(2000) // 设置抓取间隔为2秒
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
            .addHeader("Referer", "https://mp.weixin.qq.com/");

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 运行爬虫，runSpider()方法会启动一个线程，运行爬虫。
    public static void runSpider() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要爬取的微信公众号文章网址：");
        String url = scanner.nextLine();
        scanner.close();

        Spider.create(new WeChatArticleSpider()) // 创建Spider对象，传入PageProcessor实例
                .addUrl(url) // 添加用户输入的初始URL
                .thread(1) // 设置线程数为1
                .addPipeline(new ConsolePipeline()) // 添加控制台输出Pipeline
                .run(); // 启动爬虫
    }

    @Override
    // 解析页面,process()方法会根据页面的URL和内容进行解析，并返回解析结果。
    public void process(Page page) {
        try {
            // 首先获取并验证 URL
            String url = page.getUrl().toString();
            if (url == null || url.isEmpty() || url.equals("javascript:;")) {
                page.setSkip(true);
                return;
            }

            Document doc = Jsoup.parse(page.getHtml().get());
            
            // 检测微信失效链接
            Element invalidLinkElement = doc.selectFirst("div.weui-msg__title.warn");
            if (invalidLinkElement != null && invalidLinkElement.text().contains("临时链接已失效")) {
                // 设置失效链接标志
                page.putField("isInvalidLink", true);
                page.putField("url", url);
                page.putField("invalidReason", "临时链接已失效");
                // 跳过后续处理
                page.setSkip(true);
                return;
            }

            // 提取完整HTML内容
            Element contentElem = doc.selectFirst("div.rich_media_content");
            if (contentElem == null) {
                contentElem = doc.selectFirst("div#js_content");
            }
            if (contentElem == null) {
                page.setSkip(true);
                return;
            }

            // 保存原始HTML
            Element originalContentElem = contentElem.clone();
            String fullHtml = cleanHtml(originalContentElem);
            if (fullHtml.isEmpty()) {
                page.setSkip(true);
                return;
            }

            // 分析正文中的图片，为每张图片生成唯一标识符并记录位置
            Elements imgs = contentElem.select("img[data-src], img[src]");
            Map<String, Object> imageInfo = new HashMap<>();
            // 存储图片URL和对应的ULID
            Map<String, String> imageUrlToUlidMap = new HashMap<>();
            
            System.out.println("在内容中找到 " + imgs.size() + " 张图片需要处理");
            
            // 先生成所有图片的ULID，以便稍后下载
            for (int i = 0; i < imgs.size(); i++) {
                Element img = imgs.get(i);
                String imgUrl = img.attr("data-src");
                if (imgUrl.isEmpty()) {
                    imgUrl = img.attr("src");
                }
                
                if (!imgUrl.isEmpty()) {
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    
                    String cleanedUrl = cleanWeChatUrl(imgUrl);
                    String imageId = UlidUtils.generate();
                    System.out.println("为图片 #" + i + " 生成ID: " + imageId);
                    
                    // 记录图片位置、原始URL和ID映射
                    Map<String, Object> imgData = new HashMap<>();
                    imgData.put("index", i);
                    imgData.put("id", imageId);
                    imgData.put("originalUrl", cleanedUrl);
                    
                    // 添加到映射信息
                    imageInfo.put(imageId, imgData);
                    
                    // 记录URL到ULID的映射，以便下载时使用
                    imageUrlToUlidMap.put(cleanedUrl, imageId);
                    
                    // 从URL预先获取扩展名并记录，确保一致性
                    String predictedExtension = getImageExtension(cleanedUrl);
                    imageInfo.put(imageId + "_ext", predictedExtension);
                    System.out.println("预测图片 #" + i + " 扩展名: " + predictedExtension);
                }
            }
            
            // 替换内容中的图片为占位符
            for (int i = 0; i < imgs.size(); i++) {
                Element img = imgs.get(i);
                String imgUrl = img.attr("data-src");
                if (imgUrl.isEmpty()) {
                    imgUrl = img.attr("src");
                }
                
                if (!imgUrl.isEmpty()) {
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    
                    String cleanedUrl = cleanWeChatUrl(imgUrl);
                    String imageId = imageUrlToUlidMap.get(cleanedUrl);
                    if (imageId != null) {
                        // 创建图片在HTML中的标记
                        String placeholder = "[[IMG:" + imageId + "]]";
                        System.out.println("图片 #" + i + " 占位符: " + placeholder);
                        
                        // 替换HTML中的图片为占位符
                        img.after(placeholder);
                        img.remove();
                        
                        System.out.println("图片 #" + i + " 替换完成");
                    }
                }
            }
            
            // 保存处理后的文本内容
            String processedContent = contentElem.html();
            
            // 调试 - 检查处理后的内容是否包含占位符
            int placeholderCount = 0;
            for (String imageId : imageInfo.keySet()) {
                String placeholder = "[[IMG:" + imageId + "]]";
                if (processedContent.contains(placeholder)) {
                    placeholderCount++;
                }
            }
            System.out.println("处理后的内容中找到 " + placeholderCount + " 个占位符");
            
            // 保存图片映射信息
            String imageInfoJson = objectMapper.writeValueAsString(imageInfo);
            System.out.println("图片映射信息JSON大小: " + imageInfoJson.length() + " 字节");

            // 提取标题
            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) {
                title = doc.title();
            }
            if (title.isEmpty()) {
                page.setSkip(true);
                return;
            }

            // 提取作者
            String author = extractAuthor(doc);
            if (author.isEmpty()) {
                author = doc.select("div.profile_nickname").text();
            }

            // 提取公众号名称
            String accountName = extractAccountName(doc);

            // 提取发布时间
            String publishTime = extractPublishTime(doc);

            // 提取正文
            String content = contentElem.text().trim();
            if (content.isEmpty()) {
                page.setSkip(true);
                return;
            }

            // 提取图片
            List<String> imageUrls = new ArrayList<>();
            for (Element img : imgs) {
                String imgUrl = img.attr("data-src");
                if (imgUrl.isEmpty()) {
                    imgUrl = img.attr("src");
                }
                if (!imgUrl.isEmpty()) {
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    imageUrls.add(cleanWeChatUrl(imgUrl));
                }
            }

            // 提取原文链接
            String originalUrl = extractOriginalUrl(doc, url);

            // 提取文章头图
            String headImageUrl = extractHeadImage(doc);
            
            // 为文章生成ULID
            String articleUlid = UlidUtils.generate();
            
            // 存储图片ULID映射
            Map<String, String> imageUlidMap = new HashMap<>();
            
            // 新增：存储原始URL到本地路径的映射
            Map<String, String> originalUrlToLocalPathMap = new HashMap<>();
            
            // 存储结果
            page.putField("url", url);
            page.putField("sourceUrl", originalUrl);
            page.putField("title", title);
            page.putField("author", author);
            page.putField("accountName", accountName);
            page.putField("publishTime", publishTime);
            page.putField("content", processedContent);  // 使用处理后的内容
            page.putField("imageUrls", imageUrls);
            page.putField("headImageUrl", headImageUrl);
            page.putField("fullHtml", fullHtml);
            page.putField("ulid", articleUlid);  // 添加文章ULID
            page.putField("imageInfo", imageInfoJson);  // 添加图片映射信息

            // 创建以ULID命名的文件夹并下载图片
            if (!imageUrls.isEmpty() || headImageUrl != null) {
                // 首先确保image目录存在
                File imageRootFolder = new File("image");
                if (!imageRootFolder.exists()) {
                    imageRootFolder.mkdirs();
                }
                
                // 使用文章ULID创建子文件夹
                File articleFolder = new File(imageRootFolder, articleUlid);
                if (!articleFolder.exists()) {
                    articleFolder.mkdirs();
                }

                // 下载头图（如果存在）
                if (headImageUrl != null && !headImageUrl.isEmpty()) {
                    try {
                        String headImageUlid = UlidUtils.generate();
                        System.out.println("为头图生成ULID: " + headImageUlid);
                        
                        // 记录头图URL到ULID的映射
                        imageUrlToUlidMap.put(headImageUrl, headImageUlid);
                        
                        // 获取头图扩展名
                        String headImageExtension = getImageExtension(headImageUrl);
                        String headImageFileName = headImageUlid + headImageExtension;
                        
                        // 记录头图映射信息
                        Map<String, Object> headImgData = new HashMap<>();
                        headImgData.put("index", "head");
                        headImgData.put("id", headImageUlid);
                        headImgData.put("originalUrl", headImageUrl);
                        headImgData.put("extension", headImageExtension);
                        imageInfo.put(headImageUlid, headImgData);
                        
                        // 记录头图扩展名，确保内外一致
                        imageInfo.put(headImageUlid + "_ext", headImageExtension);
                        
                        imageUlidMap.put("head", headImageUlid);
                        imageUlidMap.put("head_ext", headImageExtension);
                        
                        // 新增：记录头图URL到本地路径的映射
                        String headImageLocalPath = "/image/" + articleUlid + "/" + headImageFileName;
                        originalUrlToLocalPathMap.put(headImageUrl, headImageLocalPath);
                        
                        System.out.println("正在下载文章头图: " + headImageUrl);
                        downloadImage(headImageUrl, new File(articleFolder, headImageFileName));
                        System.out.println("文章头图下载完成: " + headImageFileName);
                    } catch (IOException e) {
                        System.err.println("下载头图失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // 下载文章内容中的图片，使用与占位符一致的ULID
                List<String> imageUlids = new ArrayList<>();
                for (int i = 0; i < imageUrls.size(); i++) {
                    try {
                        String imageUrl = imageUrls.get(i);
                        // 使用之前生成的ULID，确保与占位符一致
                        String imageId = imageUrlToUlidMap.get(imageUrl);
                        if (imageId == null) {
                            // 如果没有找到映射，说明这张图片不在内容中，生成新的ULID
                            imageId = UlidUtils.generate();
                            System.out.println("图片URL " + imageUrl + " 未在内容中找到，生成新ID: " + imageId);
                        } else {
                            System.out.println("使用内容占位符中的图片ID: " + imageId + " 下载图片: " + imageUrl);
                        }
                        
                        // 优先使用之前预测的扩展名，确保一致性
                        String imageExtension;
                        Object predictedExt = imageInfo.get(imageId + "_ext");
                        if (predictedExt != null) {
                            imageExtension = predictedExt.toString();
                            System.out.println("使用预测的扩展名: " + imageExtension + " 用于图片ID: " + imageId);
                        } else {
                            // 回退到通过URL或Content-Type获取扩展名
                            imageExtension = getImageExtension(imageUrl);
                            System.out.println("找不到预测扩展名，通过URL获取扩展名: " + imageExtension + " 用于图片ID: " + imageId);
                        }
                        
                        String imageFileName = imageId + imageExtension;
                        imageUlids.add(imageId);
                        imageUlidMap.put(String.valueOf(i), imageId);
                        imageUlidMap.put(String.valueOf(i) + "_ext", imageExtension);
                        
                        // 新增：记录图片URL到本地路径的映射
                        String imageLocalPath = "/image/" + articleUlid + "/" + imageFileName;
                        originalUrlToLocalPathMap.put(imageUrl, imageLocalPath);
                        
                        downloadImage(imageUrl, new File(articleFolder, imageFileName));
                        System.out.println("图片 " + imageUrl + " 下载完成，保存为: " + imageFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                // 将图片ULID列表添加到Page对象
                page.putField("imageUlids", imageUlids);
                page.putField("imageUlidMap", imageUlidMap);
                
                // 新增：将URL映射关系添加到Page对象
                String urlMappingJson = objectMapper.writeValueAsString(originalUrlToLocalPathMap);
                page.putField("urlMapping", urlMappingJson);
            }
        } catch (Exception e) {
            page.setSkip(true);
            e.printStackTrace();
        }
    }

    private String extractAccountName(Document doc) {
        String accountName = "";
        System.out.println("开始提取公众号名称...");
        
        try {
            // 1. 从profile_meta区域提取
            Element profileMeta = doc.selectFirst("div.profile_meta_value");
            if (profileMeta != null) {
                accountName = profileMeta.text().trim();
                System.out.println("从profile_meta提取到公众号名称: " + accountName);
                if (!accountName.isEmpty()) {
                    return accountName;
                }
            }
            
            // 2. 从nickname提取
            Element nickname = doc.selectFirst("strong.profile_nickname");
            if (nickname != null) {
                accountName = nickname.text().trim();
                System.out.println("从profile_nickname提取到公众号名称: " + accountName);
                if (!accountName.isEmpty()) {
                    return accountName;
                }
            }
            
            // 3. 从其他可能的位置提取
            String[] selectors = {
                "a#js_name",
                "div.profile_nickname",
                "div#js_profile_qrcode > div > strong",
                "div.rich_media_meta_list span.rich_media_meta_nickname"
            };
            
            for (String selector : selectors) {
                Element element = doc.selectFirst(selector);
                if (element != null) {
                    accountName = element.text().trim();
                    System.out.println("使用选择器 " + selector + " 提取到公众号名称: " + accountName);
                    if (!accountName.isEmpty()) {
                        return accountName;
                    }
                }
            }
            
            System.out.println("未找到公众号名称");
            return "";
            
        } catch (Exception e) {
            System.out.println("提取公众号名称时发生错误: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // 提取发布时间，有点bug，已修改
    // 
    private String extractPublishTime(Document doc) {
        String publishTime = "";
        
        try {
            // 1. 直接从页面源码中搜索时间戳（微信文章常用方式）
            String html = doc.html();
            Pattern[] timePatterns = {
                Pattern.compile("var publish_time\\s*=\\s*\"([^\"]+)\""),
                Pattern.compile("publish_time\\s*:\\s*\"([^\"]+)\""),
                Pattern.compile("createTime\\s*=\\s*'([^']+)'"),
                Pattern.compile("ct\\s*=\\s*\"([^\"]+)\""),
                Pattern.compile("\"publish_time\"\\s*:\\s*\"([^\"]+)\""),
                Pattern.compile("data-timestamp=\"([^\"]+)\"")
            };
            
            for (Pattern pattern : timePatterns) {
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    String time = matcher.group(1).trim();
                    if (isValidDateTimeFormat(time)) {
                        return formatDateTime(time);
                    }
                }
            }

            // 2. 使用微信文章特定的选择器，主要解决办法，适配微信的多种选择器，用以针对不同的页面结构
            String[] selectors = {
                "div#publish_time",                              // 标准发布时间div
                "#publish_time",                                 // 发布时间ID
                "em#publish_time",                              // em标签中的发布时间
                "span#publish_time",                            // span标签中的发布时间
                "div.rich_media_meta_list span.rich_media_meta_text:not(.rich_media_meta_nickname)",  // 排除昵称的时间标签
                "div#meta_content span.rich_media_meta_text",   // 元数据区域的时间
                "div.rich_media_meta_list > span:nth-child(2)", // 第二个span通常是时间
                "span.rich_media_meta_text:not(:contains(RUNOOB))", // 排除包含RUNOOB的标签
                "div.rich_media_meta > p.rich_media_meta_text", // 另一种时间格式
                "#js_detail div.rich_media_meta_list em.rich_media_meta_text" // 详情区域中的时间
            };

            // 遍历所有选择器
            for (String selector : selectors) {
                Elements elements = doc.select(selector);
                for (Element element : elements) {
                    String text = element.text().trim();
                    // 过滤掉明显不是时间的内容
                    if (!text.equals("RUNOOB") && isValidDateTimeFormat(text)) {
                        return formatDateTime(text);
                    }
                }
            }

            // 3. 尝试从meta标签获取（更多meta标签组合）
            String[] metaSelectors = {
                "meta[property=article:published_time]",
                "meta[name=article:published_time]",
                "meta[property=og:publish_time]",
                "meta[name=publish_time]",
                "meta[itemprop=datePublished]",
                "meta[property=article:modified_time]"
            };

            for (String metaSelector : metaSelectors) {
                Element meta = doc.selectFirst(metaSelector);
                if (meta != null) {
                    String content = meta.attr("content");
                    if (isValidDateTimeFormat(content)) {
                        return formatDateTime(content);
                    }
                }
            }

            // 4. 如果都没找到，记录详细日志
            System.out.println("警告：无法提取发布时间，尝试的选择器结果：");
            System.out.println("=== 页面源码时间戳搜索 ===");
            for (Pattern pattern : timePatterns) {
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    System.out.println("- Pattern " + pattern + ": " + matcher.group(1));
                }
            }
            
            System.out.println("=== DOM选择器搜索 ===");
            for (String selector : selectors) {
                String result = doc.select(selector).text();
                System.out.println("- " + selector + ": " + (result.isEmpty() ? "未找到" : result));
            }

            return null;  // 返回null而不是当前时间
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("提取发布时间时发生错误：" + e.getMessage());
            return null;  // 返回null而不是当前时间
        }
    }

    // 优化验证日期时间格式的方法
    private boolean isValidDateTimeFormat(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // 过滤明显不是日期的内容
        if (text.equals("RUNOOB") || text.length() < 8) {
            return false;
        }

        // 检查更多日期时间格式
        return text.matches(".*\\d{4}[-年](0?[1-9]|1[0-2])[-月](0?[1-9]|[12]\\d|3[01])[日]?.*") || // 中文格式
               text.matches(".*\\d{4}-\\d{2}-\\d{2}.*") ||                                         // 标准日期格式
               text.matches(".*\\d{4}/\\d{2}/\\d{2}.*") ||                                         // 斜杠分隔格式
               text.matches("\\d{10,13}") ||                                                        // 时间戳格式
               text.contains("T") ||                                                                // ISO格式
               text.matches(".*\\d{4}年.*") ||                                                     // 简单中文年份
               text.matches("\\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}(:\\d{2})?.*");              // 完整时间戳格式
    }

    private String formatDateTime(String dateStr) {
        try {
            // 移除所有空格和特殊字符
            dateStr = dateStr.trim().replaceAll("[\\u00A0\\s]+", " ");
            
            // 如果日期字符串包含T，说明是ISO格式
            if (dateStr.contains("T")) {
                LocalDateTime dt = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            
            // 处理中文日期格式
            dateStr = dateStr.replaceAll("[年月]", "-")
                           .replace("日", "")
                           .replaceAll("\\s+", " ")
                           .trim();
            
            // 处理时间戳格式（10位或13位数字）
            if (dateStr.matches("\\d{10,13}")) {
                long timestamp = Long.parseLong(dateStr);
                if (dateStr.length() == 13) {
                    timestamp = timestamp / 1000;
                }
                LocalDateTime dt = LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.ofHours(8));
                return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            
            // 标准化日期格式
            if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}.*")) {
                String[] parts = dateStr.split("[-\\s]");
                // 构建基本日期部分
                StringBuilder formattedDate = new StringBuilder();
                formattedDate.append(String.format("%s-%02d-%02d", 
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                ));

                // 处理时间部分
                if (parts.length > 3) {
                    String[] timeParts = parts[3].split(":");
                    // 补全时间格式
                    StringBuilder time = new StringBuilder();
                    time.append(String.format("%02d", Integer.parseInt(timeParts[0]))); // 小时
                    time.append(":");
                    time.append(String.format("%02d", Integer.parseInt(timeParts[1]))); // 分钟
                    if (timeParts.length > 2) {
                        time.append(":").append(String.format("%02d", Integer.parseInt(timeParts[2]))); // 秒
                    } else {
                        time.append(":00"); // 补充秒
                    }
                    formattedDate.append(" ").append(time);
                } else {
                    formattedDate.append(" 00:00:00");
                }
                
                dateStr = formattedDate.toString();
            }

            // 尝试不同的日期时间格式
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);
                    return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    continue;
                }
            }

            // 如果所有格式都失败，则使用默认格式
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    
        } catch (Exception e) {
            System.out.println("日期格式化失败，原始日期字符串: " + dateStr);
            e.printStackTrace();
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    // 微信URL清洗方法
    private String cleanWeChatUrl(String url) {
        // 移除微信的图片尺寸参数
        Pattern pattern = Pattern.compile("(https://mmbiz\\.qpic\\.cn/[^?]+)\\?.*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    // 下载图片方法
    // 图片下载到image文件夹中
    private void downloadImage(String imageUrl, File outputFile) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    // 提取作者信息
    // 参考老师指导，将公众号的原作者也爬下来，区分原创和转载。
    /*
     * 参考示例：
     * https://mp.weixin.qq.com/s/YTZrwg8_W9sLfSUGOMzkqg      有阅读原文
     * https://mp.weixin.qq.com/s/vw8lXAAwz3x1RnC6T3Twcw      有作者
     * https://mp.weixin.qq.com/s/LPDETm1sUGlm6cRP1wM9Hg      有作者，但没有超链接
     * https://mp.weixin.qq.com/s/ftlipzoRkvi4OQM_TY3Pyg      没有作者
     */
    private String extractAuthor(Document doc) {
        String author = "";
        System.out.println("开始提取作者信息...");
        
        try {
            // 1. 从meta_content区域提取作者信息
            Elements metaSpans = doc.select("div#meta_content span.rich_media_meta_text");
            for (Element span : metaSpans) {
                String text = span.text().trim();
                System.out.println("找到meta_content span文本: " + text);
                // 检查是否包含"作者"关键字
                if (text.startsWith("作者")) {
                    author = text.substring(text.indexOf("作者") + 2).trim();
                    System.out.println("从meta_content提取到作者(带作者前缀): " + author);
                    return author;
                }
                // 如果是第一个非空的span，且不是"原创"，可能就是作者
                if (text.length() > 0 && !text.equals("原创")) {
                    author = text.trim();
                    System.out.println("从meta_content提取到作者(无前缀): " + author);
                    return author;
                }
            }
            
            // 2. 从文章署名提取
            Elements contentParagraphs = doc.select("div#js_content p");
            for (Element p : contentParagraphs) {
                String text = p.text().trim();
                System.out.println("检查文章内容段落: " + text);
                if (text.contains("作者：") || text.contains("作者:")) {
                    author = text.substring(text.indexOf("作者") + 2)
                               .replace("：", "")
                               .replace(":", "")
                               .trim();
                    System.out.println("从文章内容提取到作者: " + author);
                    return author;
                }
            }
            
            // 3. 从其他可能的位置提取
            String[] selectors = {
                "span.rich_media_meta_text:not(:contains(原创))",
                "div#meta_content span:not(.rich_media_meta_nickname):not(:contains(原创))",
                "span.rich_media_meta_nickname",
                "strong.profile_nickname",
                "a#js_name"
            };
            
            for (String selector : selectors) {
                Elements elements = doc.select(selector);
                for (Element element : elements) {
                    String text = element.text().trim();
                    System.out.println("使用选择器 " + selector + " 找到文本: " + text);
                    if (!text.isEmpty() && !text.equals("原创") && !text.equals("卢克文工作室")) {
                        author = text;
                        System.out.println("使用选择器提取到作者: " + author);
                        return author;
                    }
                }
            }
            
            System.out.println("未找到作者信息");
            return "";
            
        } catch (Exception e) {
            System.out.println("提取作者信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // 提取原文链接
    // 参考老师指导，将公众号的原文链接也爬下来。
    // 参考示例：
    /*
     * https://mp.weixin.qq.com/s/YTZrwg8_W9sLfSUGOMzkqg      有阅读原文
     * https://mp.weixin.qq.com/s/vw8lXAAwz3x1RnC6T3Twcw      有作者
     * https://mp.weixin.qq.com/s/LPDETm1sUGlm6cRP1wM9Hg      有作者，但没有超链接
     * https://mp.weixin.qq.com/s/ftlipzoRkvi4OQM_TY3Pyg      没有作者
     */
    private String extractOriginalUrl(Document doc, String currentUrl) {
        try {
            // 1. 首先尝试从页面源码中提取msg_source_url变量
            String html = doc.html();
            Pattern[] sourceUrlPatterns = {
                Pattern.compile("var\\s+msg_source_url\\s*=\\s*['\"]([^'\"]+)['\"]"),
                Pattern.compile("msg_source_url\\s*=\\s*['\"]([^'\"]+)['\"]"),
                Pattern.compile("source_url\\s*:\\s*['\"]([^'\"]+)['\"]"),
                Pattern.compile("msg_link\\s*=\\s*['\"]([^'\"]+)['\"]")
            };

            for (Pattern pattern : sourceUrlPatterns) {
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    String sourceUrl = matcher.group(1).trim();
                    if (!sourceUrl.isEmpty() && !sourceUrl.equals("javascript:;") && !sourceUrl.equals("javascript:void(0);")) {
                        System.out.println("找到原文链接(JavaScript变量): " + sourceUrl);
                        return sourceUrl;
                    }
                }
            }

            // 2. 尝试获取微信特殊的原文链接
            Element viewSource = doc.selectFirst("#js_view_source");
            if (viewSource != null) {
                // 检查data-url属性
                String dataUrl = viewSource.attr("data-url");
                if (!dataUrl.isEmpty() && !dataUrl.equals("javascript:;")) {
                    System.out.println("找到原文链接(data-url): " + dataUrl);
                    return dataUrl;
                }
                
                // 检查data-linktype属性
                if (viewSource.hasAttr("data-linktype")) {
                    Element metaUrl = doc.selectFirst("meta[property=og:url]");
                    if (metaUrl != null) {
                        String contentUrl = metaUrl.attr("content");
                        if (contentUrl != null && !contentUrl.isEmpty()) {
                            System.out.println("找到原文链接(meta og:url): " + contentUrl);
                            return contentUrl;
                        }
                    }
                }
            }

            // 3. 尝试从页面元数据中获取原文链接
            String[] metaSelectors = {
                "meta[property=og:url]",
                "meta[name=original-source]",
                "link[rel=canonical]",
                "meta[property=article:original_link]"
            };

            for (String selector : metaSelectors) {
                Element meta = doc.selectFirst(selector);
                if (meta != null) {
                    String url = meta.attr("content");
                    if (url.isEmpty()) {
                        url = meta.attr("href");
                    }
                    if (!url.isEmpty() && !url.equals("javascript:;")) {
                        System.out.println("找到原文链接(meta): " + url);
                        return url;
                    }
                }
            }

            // 4. 如果都没找到有效链接，记录日志并返回空字符串
            System.out.println("警告：未找到有效的原文链接");
            System.out.println("当前URL: " + currentUrl);
            System.out.println("页面内容片段: " + html.substring(0, Math.min(html.length(), 1000)));
            
            return "";
            
        } catch (Exception e) {
            System.out.println("提取原文链接时发生错误：" + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 清理HTML内容
     * 1. 移除script标签
     * 2. 移除style标签
     * 3. 移除注释
     * 4. 保留核心标签(p/img/blockquote等)
     * 5. 移除空节点
     */
    private String cleanHtml(Element element) {
        // 深度复制以避免修改原始文档
        Element cleanElement = element.clone();
        
        // 移除统计脚本和其他无关标签
        cleanElement.select("script, style, iframe, noscript").remove();
        
        // 移除空节点
        removeEmptyNodes(cleanElement);
        
        // 处理图片标签
        Elements imgs = cleanElement.select("img");
        for (Element img : imgs) {
            // 确保图片使用绝对URL
            String imgUrl = img.attr("data-src");
            if (imgUrl.isEmpty()) {
                imgUrl = img.attr("src");
            }
            if (!imgUrl.isEmpty()) {
                if (!imgUrl.startsWith("http")) {
                    imgUrl = "https:" + imgUrl;
                }
                img.attr("src", cleanWeChatUrl(imgUrl));
            }
            // 移除多余属性，只保留src和alt
            for (String attrName : img.attributes().asList().stream()
                    .map(attr -> attr.getKey())
                    .filter(key -> !key.equals("src") && !key.equals("alt"))
                    .collect(java.util.stream.Collectors.toList())) {
                img.removeAttr(attrName);
            }
        }
        
        // 添加微信特定属性
        cleanElement.select("p").attr("data-w-e-type", "paragraph");
        
        return cleanElement.outerHtml();
    }

    /**
     * 递归移除空节点
     */
    private void removeEmptyNodes(Element element) {
        for (Element child : element.children()) {
            removeEmptyNodes(child);
        }
        
        // 如果元素没有文本内容且没有图片，且不是保留的核心标签，则移除
        if (element.text().trim().isEmpty() 
            && element.select("img").isEmpty()
            && !element.tagName().matches("^(p|blockquote|h[1-6]|ul|ol|li|table|tr|td|th)$")) {
            element.remove();
        }
    }

    /**
     * 提取文章头图URL
     * 微信公众号文章头图通常在meta标签中
     */
    private String extractHeadImage(Document doc) {
        System.out.println("开始提取文章头图...");
        
        try {
            // 1. 从og:image元标签获取
            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null) {
                String imageUrl = ogImage.attr("content");
                if (!imageUrl.isEmpty()) {
                    imageUrl = cleanWeChatUrl(imageUrl);
                    System.out.println("从meta[property=og:image]提取到头图: " + imageUrl);
                    return imageUrl;
                }
            }
            
            // 2. 尝试其他可能的头图meta标签
            String[] metaSelectors = {
                "meta[name=twitter:image]",
                "meta[itemprop=image]",
                "meta[name=thumbnail]",
                "meta[name=image]"
            };
            
            for (String selector : metaSelectors) {
                Element meta = doc.selectFirst(selector);
                if (meta != null) {
                    String imageUrl = meta.attr("content");
                    if (!imageUrl.isEmpty()) {
                        imageUrl = cleanWeChatUrl(imageUrl);
                        System.out.println("从" + selector + "提取到头图: " + imageUrl);
                        return imageUrl;
                    }
                }
            }
            
            // 3. 查找页面中第一张图片作为头图（备选方案）
            Element firstImg = doc.selectFirst("div.rich_media_content img, div#js_content img");
            if (firstImg != null) {
                String imgUrl = firstImg.attr("data-src");
                if (imgUrl.isEmpty()) {
                    imgUrl = firstImg.attr("src");
                }
                if (!imgUrl.isEmpty()) {
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    imgUrl = cleanWeChatUrl(imgUrl);
                    System.out.println("从文章内容提取第一张图片作为头图: " + imgUrl);
                    return imgUrl;
                }
            }
            
            System.out.println("未找到文章头图");
            return null;
            
        } catch (Exception e) {
            System.out.println("提取文章头图时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从图片URL或HTTP响应获取图片扩展名
     * @param imageUrl 图片URL
     * @return 图片扩展名（如.jpg, .png, .gif）
     */
    private String getImageExtension(String imageUrl) {
        // 默认扩展名
        String defaultExtension = ".jpg";
        
        try {
            // 1. 尝试从URL中获取扩展名
            if (imageUrl.contains(".")) {
                String urlPath = new URL(imageUrl).getPath();
                int lastDotIndex = urlPath.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    String extension = urlPath.substring(lastDotIndex).toLowerCase();
                    // 检查是否为合法图片扩展名
                    if (extension.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
                        return extension;
                    }
                }
            }
            
            // 2. 尝试从HTTP头获取Content-Type
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            
            String contentType = connection.getContentType();
            connection.disconnect();
            
            if (contentType != null) {
                switch (contentType.toLowerCase()) {
                    case "image/jpeg":
                        return ".jpg";
                    case "image/png":
                        return ".png";
                    case "image/gif":
                        return ".gif";
                    case "image/webp":
                        return ".webp";
                    case "image/bmp":
                        return ".bmp";
                }
            }
        } catch (Exception e) {
            System.err.println("获取图片扩展名失败: " + e.getMessage());
        }
        
        return defaultExtension;
    }

    // Site对象，配置爬虫User-Agent
    @Override
    public Site getSite() {
        return site;
    }
}
