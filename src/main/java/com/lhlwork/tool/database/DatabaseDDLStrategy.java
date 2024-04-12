package com.lhlwork.tool.database;

import java.sql.SQLException;
import java.sql.Statement;

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
     * 删除数据库
     *
     * @param statement statement
     * @param database  database
     * @throws SQLException sql exception
     */
    void dropDatabase(Statement statement, String database) throws SQLException;

    void createTable(Statement statement, String database, String table) throws SQLException;
}
