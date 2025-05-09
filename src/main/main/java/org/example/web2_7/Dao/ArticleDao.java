package org.example.web2_7.Dao;
/*
 * 文章Dao层
 * 用途：
 * 1. 数据库操作 - 与数据库表article_table的字段一一对应
 * 2. 数据传输 - 在各层之间传递文章数据
 * 3. 前端展示 - 提供给前端展示所需的数据结构
 */
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.web2_7.pojo.Article;

@Mapper
public interface ArticleDao {
    @Select("SELECT * FROM article_table WHERE id = #{id} AND is_deleted = false")
    Article findById(Integer id);
} 