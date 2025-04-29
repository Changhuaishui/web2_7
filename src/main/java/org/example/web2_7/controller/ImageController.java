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
            } else {
                // 如果没有扩展名，尝试从article.images中查找
                String images = article.getImages();
                if (images != null && !images.isEmpty()) {
                    String[] imagePaths = images.split(",");
                    for (String path : imagePaths) {
                        if (path.contains(imageUlid)) {
                            // 找到匹配的图片路径
                            int extIndex = path.lastIndexOf('.');
                            if (extIndex > 0) {
                                extension = path.substring(extIndex);
                                break;
                            }
                        }
                    }
                }
                // 添加默认扩展名
                imageUlid = imageUlid + extension;
            }

            // 构建图片路径
            Path imagePath = Paths.get(IMAGE_ROOT_DIR, articleUlid, imageUlid);
            File file = imagePath.toFile();

            // 验证图片是否存在
            if (!file.exists() || !file.isFile()) {
                logger.warn("图片文件不存在: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            // 返回图片资源
            Resource resource = new FileSystemResource(file);
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

            logger.info("图片获取成功: {}, Content-Type: {}", imagePath, mediaType);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取图片时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 