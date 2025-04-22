package org.example.web2_7.service;
/*
 * @author: chen
 * 添加配置和优化搜索，优化搜索结果，
 * 使用Lucene实现全文检索功能
 * 1. 添加文档到索引
 * 2. 搜索文档
 * 3. 删除文档
 * 4. 重建索引
 * 5. 关闭资源
 *
 * --------------------------------
 * 
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.example.web2_7.Dao.ArticleMapper;
import org.example.web2_7.pojo.Article;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneIndexService {
    @Autowired
    private ArticleMapper articleMapper;

    @Value("${lucene.index.directory:lucene/indexes}")
    private String indexDir;

    private Directory directory;
    private StandardAnalyzer analyzer;
    private IndexWriter writer;

    @PostConstruct
    public void init() throws IOException {
        Path indexPath = Paths.get(indexDir);
        directory = FSDirectory.open(indexPath);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, config);
    }

    public void addToIndex(String title, String content, String url) throws Exception {
        Document doc = new Document();
        
        // 存储并索引标
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        
        // 存储并索引内容
        doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
        
        // 存储并索引URL
        doc.add(new TextField("url", url != null ? url : "", Field.Store.YES));
        
        // 存储并索引作者和公众号名称
        doc.add(new TextField("author", getAuthorFromUrl(url), Field.Store.YES));
        doc.add(new TextField("accountName", getAccountNameFromUrl(url), Field.Store.YES));
        
        // 添加文档时间戳
        doc.add(new LongPoint("timestamp", System.currentTimeMillis()));

        writer.addDocument(doc);
        writer.commit();
    }

    private String getAuthorFromUrl(String url) {
        // 从数据库中获取作者信息
        return "";  // 这里需要实现从数据库获取作者信息的逻辑
    }

    private String getAccountNameFromUrl(String url) {
        // 从数据库中获取公众号名称
        return "";  // 这里需要实现从数据库获取公众号名称的逻辑
    }

    public List<SearchResult> search(String queryStr) throws Exception {
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // 多字段搜索
        String[] fields = {"title", "content", "author", "accountName", "url"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        Query query = parser.parse(queryStr);

        // 执行搜索，返回前50条结果
        TopDocs hits = searcher.search(query, 50);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc hit : hits.scoreDocs) {
            Document doc = searcher.doc(hit.doc);
            results.add(new SearchResult(
                doc.get("title"),
                doc.get("content"),
                doc.get("url"),
                doc.get("author"),
                doc.get("accountName"),
                hit.score
            ));
        }

        reader.close();
        return results;
    }

    public static class SearchResult {
        public final String title;
        public final String content;
        public final String url;
        public final String author;
        public final String accountName;
        public final float score;

        public SearchResult(String title, String content, String url, String author, String accountName, float score) {
            this.title = title;
            this.content = content;
            this.url = url;
            this.author = author;
            this.accountName = accountName;
            this.score = score;
        }
    }

    // 清理资源
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (directory != null) {
            directory.close();
        }
    }

    public void deleteFromIndex(String url) throws Exception {
        Term term = new Term("url", url);
        writer.deleteDocuments(term);
        writer.commit();
    }

    public void rebuildIndex() throws Exception {
        // 删除所有现有索引
        writer.deleteAll();
        
        // 从数据库获取所有未被逻辑删除的文章并重新索引
        List<Article> articles = articleMapper.findAllOrderByPublishTime();
        for (Article article : articles) {
            // 只索引未被逻辑删除的文章
            if (article.getIsDeleted() == null || !article.getIsDeleted()) {
                addToIndex(
                    article.getTitle(),
                    article.getContent(),
                    article.getUrl()
                );
            }
        }
        
        // 提交更改
        writer.commit();
    }
}