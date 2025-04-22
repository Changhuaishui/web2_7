package org.example.web2_7.service.impl;

import org.example.web2_7.Dao.ArticleDao;
import org.example.web2_7.pojo.Article;
import org.example.web2_7.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Override
    public Article getArticleById(Integer id) {
        return articleDao.findById(id);
    }
} 