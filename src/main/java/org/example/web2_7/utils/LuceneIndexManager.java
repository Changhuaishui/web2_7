package org.example.web2_7.utils;

/*
 * @auther chen 
 * 使用Lucene实现全文检索功能。
 * 注册注解@Component，Spring管理的组件
 * 为服务层的service注入依赖提供全文检索功能
 * --------------------------------
 * 具体是：
 * 1. 创建或更新索引
 * 2. 更新单个文档
 * 3. 删除文档
 * 4. 获取IndexReader
 * 5. 关闭资源
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.example.web2_7.pojo.Article;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component  // 添加@Component注解，Spring管理的组件
public class LuceneIndexManager {
    private static final String INDEX_DIR = "lucene_index";
    private final Analyzer analyzer;
    private final Directory directory;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LuceneIndexManager() throws IOException {
        this.analyzer = new IKAnalyzer(false);  // 使用IK分词器，设置为细粒度分词模式，提高召回率
        Path indexPath = Paths.get(INDEX_DIR);  
        this.directory = FSDirectory.open(indexPath);
    }

    // 创建或更新索引
    public void createOrUpdateIndex(List<Article> articles) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // 重新创建索引
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (Article article : articles) {
                updateDocument(writer, article);
            }
        }
    }

    // 更新单个文档
    public void updateDocument(IndexWriter writer, Article article) throws IOException {
        Document doc = new Document();
        
        // 添加字段，设置是否存储和分词
        doc.add(new StringField("id", String.valueOf(article.getId()), Field.Store.YES));
        
        // 使用TextField存储并索引标题和内容，保留原字段名称以便于查询
        doc.add(new TextField("title", article.getTitle(), Field.Store.YES));
        doc.add(new TextField("content", article.getContent(), Field.Store.YES));
        
        // 为支持中文搜索，添加额外的全文字段，合并标题和内容以增强相关性
        String fullText = article.getTitle() + " " + article.getContent();
        doc.add(new TextField("fullText", fullText, Field.Store.NO));
        
        doc.add(new StringField("author", article.getAuthor() != null ? article.getAuthor() : "", Field.Store.YES));
        doc.add(new StringField("accountName", article.getAccountName() != null ? article.getAccountName() : "", Field.Store.YES));
        doc.add(new StringField("url", article.getUrl() != null ? article.getUrl() : "", Field.Store.YES));
        doc.add(new StringField("sourceUrl", article.getSourceUrl() != null ? article.getSourceUrl() : "", Field.Store.YES));
        
        // 处理发布时间，添加用于排序的SortedDocValuesField
        String publishTimeStr = article.getPublishTime() != null ? 
            article.getPublishTime().format(DATE_FORMATTER) : "";
        doc.add(new StringField("publishTime", publishTimeStr, Field.Store.YES));
        // 添加用于排序的字段
        doc.add(new SortedDocValuesField("publishTime", new BytesRef(publishTimeStr)));
        
        doc.add(new StringField("isDeleted", String.valueOf(article.getIsDeleted()), Field.Store.YES));

        // 使用updateDocument替代addDocument，确保文档不会重复
        writer.updateDocument(new Term("id", String.valueOf(article.getId())), doc);
    }

    // 删除文档
    public void deleteDocument(String id) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            writer.deleteDocuments(new Term("id", id));
        }
    }

    // 获取IndexReader
    public IndexReader getIndexReader() throws IOException {
        return DirectoryReader.open(directory);
    }

    // 关闭资源
    public void close() throws IOException {
        directory.close();
    }
} 