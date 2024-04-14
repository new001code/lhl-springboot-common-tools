package com.lhlwork.tool.database;

import com.lhlwork.anno.database.Table;
import com.lhlwork.config.ColumnProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface DatabaseDDLStrategy {


    /**
     * 查询数据库是否存在
     *
     * @return is existed
     */
    Boolean databaseExist(Statement statement, String database) throws SQLException;


    /**
     * 创建数据库
     *
     * @param statement statement
     * @param database  database
     * @throws SQLException sql exception
     */
    void createDatabase(Statement statement, String database) throws SQLException;

    /**
     * 创建表
     *
     * @param statement     statement
     * @param currentTables current tables
     * @throws SQLException sql exception
     */
    void createTables(Statement statement, Map<Table, List<ColumnProperties>> currentTables) throws SQLException;

    /**
     * 拼装建表SQL
     *
     * @return sql
     */
    String getCreateTablesSql(Map<Table, List<ColumnProperties>> currentTables);

    /**
     * 获取表名
     *
     * @param statement    statement
     * @param schemaName database name
     * @throws SQLException sql exception
     */
    Set<String> getTableNameSameSchema(Statement statement, String schemaName) throws SQLException;

    /**
     * 执行SQL
     *
     * @param statement statement
     * @param sql       sql
     * @throws SQLException sql exception
     */
    default void executeByFile(Statement statement, String sql) throws SQLException {
        Logger log = LoggerFactory.getLogger(DatabaseDDLStrategy.class);
        log.debug(sql);
        statement.execute(sql);
    }
}
