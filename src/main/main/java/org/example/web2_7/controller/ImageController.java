package org.example.web2_7.controller;

import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 图片控制器
 * 提供API根据ULID获取图片
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ArticleMapper articleMapper;

    // 基础图片目录
    private static final String IMAGE_ROOT_DIR = "image";

    /**
     * 根据图片ULID获取图片
     * URL格式: /api/images/{articleUlid}/{imageUlid}
     * 例如: /api/images/01HZW2RKF63AWC1BT901FPGRGT/01HZW2RKF63AWC1BT901FPGRGA.jpg
     */
    @GetMapping("/{articleUlid}/{imageUlid}")
    public ResponseEntity<Resource> getImage(
            @PathVariable("articleUlid") String articleUlid,
            @PathVariable("imageUlid") String imageUlid) {
        logger.info("获取图片请求: 文章ULID={}, 图片ULID={}", articleUlid, imageUlid);

        try {
            // 验证文章是否存在
            Article article = articleMapper.findByUlid(articleUlid);
            if (article == null) {
                logger.warn("文章不存在: ULID={}", articleUlid);
                return ResponseEntity.notFound().build();
            }

            // 处理图片扩展名
            String extension = ".jpg"; // 默认扩展名
            if (imageUlid.contains(".")) {
                // 如果路径中包含扩展名，保留原扩展名
                extension = imageUlid.substring(imageUlid.lastIndexOf('.'));
                // 移除扩展名，以获取纯粹的imageUlid
                imageUlid = imageUlid.substring(0, imageUlid.lastIndexOf('.'));
            }
            
            // 构建图片路径
            Path imageFolderPath = Paths.get(IMAGE_ROOT_DIR, articleUlid);
            File imageFolder = imageFolderPath.toFile();
            
            // 首先尝试精确查找指定的图片
            File exactImageFile = Paths.get(IMAGE_ROOT_DIR, articleUlid, imageUlid + extension).toFile();
            
            // 如果精确匹配的图片不存在，尝试查找不同扩展名的同一图片
            if (!exactImageFile.exists()) {
                logger.warn("指定的图片文件不存在: {}，尝试查找不同扩展名的同一图片", exactImageFile.getPath());
                
                // 尝试常见的图片扩展名
                for (String ext : new String[]{"jpg", "jpeg", "png", "gif", "webp", "bmp"}) {
                    if (extension.substring(1).equalsIgnoreCase(ext)) {
                        continue; // 跳过已尝试的扩展名
                    }
                    
                    File altExtFile = Paths.get(IMAGE_ROOT_DIR, articleUlid, imageUlid + "." + ext).toFile();
                    if (altExtFile.exists()) {
                        exactImageFile = altExtFile;
                        extension = "." + ext;
                        logger.info("找到不同扩展名的同一图片: {}", exactImageFile.getName());
                        break;
                    }
                }
            }
            
            // 如果仍未找到匹配的图片，则尝试查找类似名称的图片
            if (!exactImageFile.exists() && imageFolder.exists() && imageFolder.isDirectory()) {
                logger.warn("指定的图片文件不存在: {}，尝试查找类似名称的图片", exactImageFile.getPath());
                
                // 如果imageUlid是数字ID，可能是文章内容中的占位符ID
                final String imageId = imageUlid; // 创建final副本用于lambda
                boolean isNumericId = imageId.matches("\\d+");
                if (isNumericId) {
                    logger.info("检测到数字图片ID: {}，尝试在文章目录中查找对应的图片文件", imageId);
                    
                    // 尝试查找包含该数字ID的文件
                    File[] matchingFiles = imageFolder.listFiles((dir, name) -> 
                        (name.contains(imageId) || name.startsWith(imageId)) && 
                        (name.toLowerCase().endsWith(".jpg") || 
                         name.toLowerCase().endsWith(".jpeg") || 
                         name.toLowerCase().endsWith(".png") || 
                         name.toLowerCase().endsWith(".gif") || 
                         name.toLowerCase().endsWith(".webp")));
                    
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        exactImageFile = matchingFiles[0];
                        logger.info("找到匹配数字ID的图片: {}", exactImageFile.getName());
                    } else {
                        logger.warn("未找到匹配数字ID {}的图片文件", imageId);
                    }
                }
                
                // 如果仍未找到匹配的图片，尝试按索引查找图片
                if (!exactImageFile.exists()) {
                    logger.info("尝试按索引查找图片，目录: {}", imageFolder.getPath());
                    
                    File[] allImages = imageFolder.listFiles((dir, name) -> 
                        name.toLowerCase().endsWith(".jpg") || 
                        name.toLowerCase().endsWith(".jpeg") || 
                        name.toLowerCase().endsWith(".png") || 
                        name.toLowerCase().endsWith(".gif") || 
                        name.toLowerCase().endsWith(".webp"));
                    
                    if (allImages != null && allImages.length > 0) {
                        // 对文件按名称排序
                        Arrays.sort(allImages);
                        
                        // 尝试将imageUlid解析为索引
                        try {
                            int index = Integer.parseInt(imageUlid);
                            // 确保索引在有效范围内
                            if (index >= 0 && index < allImages.length) {
                                exactImageFile = allImages[index];
                                logger.info("按索引{}找到图片: {}", index, exactImageFile.getName());
                            } else if (allImages.length > 0) {
                                // 如果索引超出范围，使用第一张图片
                                exactImageFile = allImages[0];
                                logger.info("索引{}超出范围(0-{})，使用第一张图片: {}", 
                                           index, allImages.length-1, exactImageFile.getName());
                            }
                        } catch (NumberFormatException e) {
                            // 如果imageUlid不是有效的数字，使用第一张图片
                            if (allImages.length > 0) {
                                exactImageFile = allImages[0];
                                logger.info("无法解析图片ID为索引，使用第一张图片: {}", exactImageFile.getName());
                            }
                        }
                    }
                }
                
                // 如果仍未找到特定图片，则使用目录中的任何图片
                if (!exactImageFile.exists()) {
                    logger.warn("未找到特定图片，尝试使用目录中的任意图片");
                    
                    File[] availableImages = imageFolder.listFiles((dir, name) -> 
                        name.toLowerCase().endsWith(".jpg") || 
                        name.toLowerCase().endsWith(".jpeg") || 
                        name.toLowerCase().endsWith(".png") || 
                        name.toLowerCase().endsWith(".gif") || 
                        name.toLowerCase().endsWith(".webp"));
                    
                    if (availableImages != null && availableImages.length > 0) {
                        // 使用目录中的第一张图片作为替代
                        exactImageFile = availableImages[0];
                        logger.info("找到替代图片: {}", exactImageFile.getName());
                        
                        // 更新扩展名以匹配实际文件
                        String fileName = exactImageFile.getName();
                        if (fileName.contains(".")) {
                            extension = fileName.substring(fileName.lastIndexOf('.'));
                        }
                    }
                }
            }
            
            // 验证图片是否存在
            if (!exactImageFile.exists() || !exactImageFile.isFile()) {
                logger.warn("图片文件不存在且无法找到替代图片: {}", exactImageFile.getPath());
                return ResponseEntity.notFound().build();
            }

            // 返回图片资源
            Resource resource = new FileSystemResource(exactImageFile);
            HttpHeaders headers = new HttpHeaders();
            
            // 根据文件扩展名设置正确的Content-Type
            MediaType mediaType = MediaType.IMAGE_JPEG; // 默认类型
            String extLower = extension.toLowerCase();
            if (extLower.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (extLower.endsWith(".gif")) {
                mediaType = MediaType.valueOf("image/gif");
            } else if (extLower.endsWith(".webp")) {
                mediaType = MediaType.valueOf("image/webp");
            } else if (extLower.endsWith(".bmp")) {
                mediaType = MediaType.valueOf("image/bmp");
            }
            
            headers.setContentType(mediaType);

            logger.info("图片获取成功: {}, Content-Type: {}", exactImageFile.getPath(), mediaType);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取图片时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 