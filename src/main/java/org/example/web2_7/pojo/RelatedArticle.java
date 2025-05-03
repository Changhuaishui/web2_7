package org.example.web2_7.pojo;

import lombok.Data;
import java.util.Date;

/**
 * RelatedArticle实体类：映射 related_articles 表
 * 用于存储文章的相关文章信息
 */
@Data
public class RelatedArticle {
    private Integer id;            // 自增主键ID
    private Integer articleId;     // 源文章ID
    private String relatedUrl;     // 相关文章URL
    private String title;          // 相关文章标题
    private Date createdAt;        // 创建时间
} 