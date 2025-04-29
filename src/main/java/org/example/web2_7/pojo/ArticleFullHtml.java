package org.example.web2_7.pojo;

import lombok.Data;
import java.util.Date;

/**
 * ArticleFullHtml实体类：映射 article_full_html 表
 * 用于存储文章的完整HTML内容
 */
@Data
public class ArticleFullHtml {
    private Integer id;            // 自增主键ID
    private Integer articleId;     // 关联到article_table的ID
    private String fullHtml;       // 文章完整HTML内容
    private Date createdAt;        // 创建时间
} 