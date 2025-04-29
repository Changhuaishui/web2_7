package org.example.web2_7.Dao;
/*
 * @author chen
 * ArticleMapper属于DAO层，用于操作数据库表article_table
 * 负责数据库的访问、定义数据库操作方法、使用pojo对象
 * 用途：
 * 1. 数据库操作 - 与数据库表article_table的字段一一对应
 * 2. 数据传输 - 在各层之间传递文章数据
 * 3. 前端展示 - 提供给前端展示所需的数据结构
 * 
 * 特点：
 * 1. 使用MyBatis的注解方式操作数据库
 * 2. 使用Spring的@Transactional注解管理事务
 * 3. 使用Lombok的@Data注解简化代码
 * 4. 使用POJO类Article作为数据传输对象
 * 5. 使用@Mapper注解将接口映射到MyBatis的XML配置文件中
 * 6. 使用@Insert、@Select、@Update、@Delete注解操作数据库
 * 7. 使用@Options注解设置自动生成主键
 * 8. 使用@Param注解传递参数
 * 9. 使用@Result注解映射查询结果
 */
import org.apache.ibatis.annotations.*;
import org.example.web2_7.pojo.Article;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


/*
 * mapper注解
 * 1. @Mapper注解：将接口映射到MyBatis的XML配置文件中
 * 2. @Insert注解：插入数据
 * 3. @Select注解：查询数据
 * 4. @Update注解：更新数据
 * 5. @Delete注解：删除数据
 * 6. @Options注解：设置自动生成主键
 */
@Mapper
public interface ArticleMapper {
    // 插入文章
    @Insert("INSERT INTO article_table (ulid, title, author, url, source_url, account_name, publish_time, content, images, is_deleted) " +
            "VALUES (#{ulid}, #{title}, #{author}, #{url}, #{sourceUrl}, #{accountName}, #{publishTime}, #{content}, #{images}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertArticle(Article article);

    // 根据URL查找文章（排除已删除的）
    @Select("SELECT * FROM article_table WHERE url LIKE CONCAT(#{url}, '%') AND is_deleted = false LIMIT 1")
    Article findByUrl(String url);
    
    // 根据ULID查找文章
    @Select("SELECT * FROM article_table WHERE ulid = #{ulid} AND is_deleted = false LIMIT 1")
    Article findByUlid(String ulid);
    
    // 根据URL查找所有文章（包括已删除的）
    @Select("SELECT * FROM article_table WHERE url LIKE CONCAT(#{url}, '%') LIMIT 1")
    Article findByUrlIncludeDeleted(String url);

    // 根据公众号名称查找文章（排除已删除的）
    @Select("SELECT * FROM article_table WHERE account_name = #{accountName} AND is_deleted = false")
    List<Article> findByAccountName(String accountName);

    // 获取所有文章（按发布时间倒序，排除已删除的）
    @Select("SELECT id, ulid, title, author, url, source_url AS sourceUrl, account_name AS accountName, " +
            "publish_time AS publishTime, content, images, is_deleted AS isDeleted, summary " +
            "FROM article_table WHERE is_deleted = false " +
            "ORDER BY publish_time DESC")
    List<Article> findAllOrderByPublishTime();

    // 关键词搜索（排除已删除的）
    @Select("SELECT * FROM article_table WHERE (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%')) AND is_deleted = false")
    List<Article> searchByKeyword(String keyword);

    // 逻辑删除文章
    @Update("UPDATE article_table SET is_deleted = true WHERE url LIKE CONCAT(#{url}, '%')")
    @Transactional
    int logicalDeleteByUrl(String url);
    
    // 根据ULID逻辑删除文章
    @Update("UPDATE article_table SET is_deleted = true WHERE ulid = #{ulid}")
    @Transactional
    int logicalDeleteByUlid(String ulid);
    
    // 物理删除文章（实际从数据库删除）
    @Delete("DELETE FROM article_table WHERE url LIKE CONCAT(#{url}, '%')")
    @Transactional
    int deleteByUrl(String url);
    
    // 根据ULID物理删除文章
    @Delete("DELETE FROM article_table WHERE ulid = #{ulid}")
    @Transactional
    int deleteByUlid(String ulid);
    
    // 获取所有文章（包括已逻辑删除的，用于完全重建索引）
    @Select("SELECT * FROM article_table ORDER BY publish_time DESC")
    List<Article> findAllArticlesIncludeDeleted();

    // 根据ID获取文章
    @Select("SELECT id, ulid, title, author, url, source_url AS sourceUrl, account_name AS accountName, " +
            "publish_time AS publishTime, content, images, is_deleted AS isDeleted, summary " +
            "FROM article_table WHERE id = #{id} AND is_deleted = false LIMIT 1")
    Article findById(Integer id);

    // 插入文章HTML内容
    @Insert("INSERT INTO article_full_html (article_id, full_html) VALUES (#{articleId}, #{fullHtml})")
    int insertArticleHtml(@Param("articleId") Integer articleId, @Param("fullHtml") String fullHtml);

    // 获取文章HTML内容，使用关联表article_full_html，article_id作为查询参数
    @Select("SELECT full_html FROM article_full_html WHERE article_id = #{articleId}")
    String getArticleHtml(@Param("articleId") Integer articleId);

    // 更新文章摘要
    @Update("UPDATE article_table SET summary = #{summary} WHERE id = #{id}")
    int updateArticleSummary(@Param("id") Integer id, @Param("summary") String summary);
}
