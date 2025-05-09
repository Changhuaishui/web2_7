-- SQL脚本：自动检测并清除所有表数据
-- 文件名: 10_auto_clear_all_tables.sql
-- 创建时间: 2025-05-02
-- 描述: 此脚本会自动检测数据库中的所有表并清空其数据，但保留表结构和约束

-- 禁用外键约束检查
SET FOREIGN_KEY_CHECKS = 0;

-- 创建存储过程来动态生成和执行TRUNCATE语句
DROP PROCEDURE IF EXISTS ClearAllTables;

DELIMITER //
CREATE PROCEDURE ClearAllTables()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE tableName VARCHAR(255);
    
    -- 声明游标来获取当前数据库中所有表名
    DECLARE cur CURSOR FOR 
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = DATABASE()
        AND table_type = 'BASE TABLE';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 创建存储TRUNCATE语句的临时表
    DROP TEMPORARY TABLE IF EXISTS temp_truncate_statements;
    CREATE TEMPORARY TABLE temp_truncate_statements (
        id INT AUTO_INCREMENT PRIMARY KEY,
        sql_stmt VARCHAR(1000)
    );
    
    -- 打开游标并遍历所有表
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO tableName;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 构建TRUNCATE语句并插入临时表
        SET @sql = CONCAT('INSERT INTO temp_truncate_statements (sql_stmt) VALUES (\'TRUNCATE TABLE ', tableName, '\')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    
    CLOSE cur;
    
    -- 遍历临时表中的所有TRUNCATE语句并执行
    SET done = FALSE;
    
    -- 声明游标来获取所有TRUNCATE语句
    DECLARE truncate_cur CURSOR FOR 
        SELECT sql_stmt FROM temp_truncate_statements ORDER BY id;
    
    OPEN truncate_cur;
    
    truncate_loop: LOOP
        DECLARE truncate_stmt VARCHAR(1000);
        FETCH truncate_cur INTO truncate_stmt;
        IF done THEN
            LEAVE truncate_loop;
        END IF;
        
        -- 执行TRUNCATE语句
        SET @sql = truncate_stmt;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 输出日志
        SELECT CONCAT('Executed: ', truncate_stmt) AS 'Log';
    END LOOP;
    
    CLOSE truncate_cur;
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS temp_truncate_statements;
END //
DELIMITER ;

-- 执行存储过程
CALL ClearAllTables();

-- 删除存储过程
DROP PROCEDURE IF EXISTS ClearAllTables;

-- 重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 提示清理完成
SELECT 'All tables have been automatically cleared successfully.' AS 'Status'; 