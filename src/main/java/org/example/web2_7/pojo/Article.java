package org.example.web2_7.pojo;
/*
 * @author chen
 * Article实体类：纯粹数据载体，不包含业务逻辑，用于数据传输
 * 用途：
 * 1. 数据库映射 - 与数据库表article_table的字段一一对应
 * 2. 数据传输 - 在各层之间传递文章数据
 * 3. 前端展示 - 提供给前端展示所需的数据结构
 * @Data 注解：
 * 1. 自动生成getter和setter方法
 * 2. 自动生成toString方法
 * 3. 自动生成equals和hashCode方法
 * 4. 自动生成构造方法
 * 5. 自动生成lombok注解
 */
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

//用Lombok注解简化代码，生成getter和setter方法
@Data
public class Article {
    private Integer id;  // 文章ID  
    private String ulid;  // 使用ULID作为唯一标识符
    private String title;  // 文章标题
    private String author;  // 文章作者
    private String url;  // 文章链接
    private String sourceUrl;  // 文章来源链接
    private String accountName;  // 文章来源公众号名称

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime publishTime;  // 文章发布时间
    
    private String content;  // 文章内容
    private String images;  // 文章图片
    private Boolean isDeleted = false;  // 是否删除，逻辑删除，标记1/0
    private String summary;  // 文章摘要

    public String getUlid() {
        return ulid;
    }

    public void setUlid(String ulid) {
        this.ulid = ulid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getPublishTime() {
        return this.publishTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
