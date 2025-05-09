package org.example.web2_7.Dao;

import org.apache.ibatis.annotations.*;
import org.example.web2_7.pojo.RelatedArticle;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 相关文章数据访问接口
 * 
 */
@Mapper
public interface RelatedArticleMapper {
    
    /**
     * 插入相关文章记录
     * @param relatedArticle 相关文章对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO related_articles (article_id, related_url, title) " +
            "VALUES (#{articleId}, #{relatedUrl}, #{title})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRelatedArticle(RelatedArticle relatedArticle);
    
    /**
     * 批量插入相关文章记录（忽略重复项）
     * @param articleId 文章ID
     * @param relatedUrl 相关文章URL
     * @param title 相关文章标题
     * @return 影响的行数
     */
    @Insert("INSERT IGNORE INTO related_articles (article_id, related_url, title) " +
            "VALUES (#{articleId}, #{relatedUrl}, #{title})")
    int insertIgnore(@Param("articleId") Integer articleId, 
                    @Param("relatedUrl") String relatedUrl, 
                    @Param("title") String title);
    
    /**
     * 根据文章ID获取所有相关文章
     * @param articleId 文章ID
     * @return 相关文章列表
     */
    @Select("SELECT * FROM related_articles WHERE article_id = #{articleId}")
    List<RelatedArticle> findByArticleId(Integer articleId);
    
    /**
     * 删除文章的相关文章记录
     * @param articleId 文章ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM related_articles WHERE article_id = #{articleId}")
    @Transactional
    int deleteByArticleId(Integer articleId);
    
    /**
     * 检查相关文章是否已存在
     * @param articleId 文章ID
     * @param relatedUrl 相关文章URL
     * @return 存在的记录数
     */
    @Select("SELECT COUNT(*) FROM related_articles WHERE article_id = #{articleId} AND related_url = #{relatedUrl}")
    int existsByArticleIdAndUrl(@Param("articleId") Integer articleId, @Param("relatedUrl") String relatedUrl);
} 