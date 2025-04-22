-- 创建文章表
CREATE TABLE IF NOT EXISTS article_table (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    url VARCHAR(1000) NOT NULL,
    account_name VARCHAR(100),
    publish_time DATETIME,
    content TEXT,
    images TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE KEY unique_url (url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章HTML内容表
CREATE TABLE IF NOT EXISTS article_full_html (
    id INT AUTO_INCREMENT PRIMARY KEY,
    article_id INT NOT NULL,
    full_html LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_article_id (article_id),
    FOREIGN KEY (article_id) REFERENCES article_table(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 