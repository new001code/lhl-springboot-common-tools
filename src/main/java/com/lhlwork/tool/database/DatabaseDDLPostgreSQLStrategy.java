package com.lhlwork.tool.database;

import com.lhlwork.anno.database.Table;
import com.lhlwork.config.ColumnProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Component("org.postgresql.Driver")
@Slf4j
public class DatabaseDDLPostgreSQLStrategy implements DatabaseDDLStrategy {
    @Override
    public Boolean databaseExist(Statement statement, String database) throws SQLException {
        String query = "SELECT datname FROM pg_database WHERE datname = '%s'".formatted(database);
        ResultSet resultSet = statement.executeQuery(query);
        boolean existed = resultSet.next();
        resultSet.close();
        return existed;
    }

    @Override
    public void createDatabase(Statement statement, String database) throws SQLException {
        String query = "CREATE DATABASE %s".formatted(database);
        int executed = statement.executeUpdate(query);
        if (executed == 0) {
            log.info("create database {} success", database);
        }
    }

    @Override
    public void dropDatabase(Statement statement, String database) throws SQLException {
        String query = "DROP DATABASE %s".formatted(database);
        int executed = statement.executeUpdate(query);
        if (executed == 0) {
            log.info("drop database {} success", database);
        }

    }

    @Override
    public void createTables(Statement statement, Map<Table, List<ColumnProperties>> currentTables) throws SQLException {
        String sql = this.getCreateTablesSql(currentTables);
        log.debug(sql);
        int executed = statement.executeUpdate(sql);
        if (executed == 0) {
            log.info("create tables success");
        }

    }

    @Override
    public String getCreateTablesSql(Map<Table, List<ColumnProperties>> currentTables) {
        StringBuilder sql = new StringBuilder();
        StringBuilder comment = new StringBuilder();

        currentTables.forEach((table, columnProperties) -> {
            String tableName = table.tableName();
            sql.append("DROP TABLE IF EXISTS %s;".formatted(tableName));
            sql.append("CREATE TABLE %s(".formatted(tableName));
            columnProperties.forEach(column -> {
                sql.append("%s ".formatted(column.getName()));
                // 是否自增
                if (column.getIsAutoIncrement()) {
                    sql.append("SERIAL ");
                } else {
                    //非自增则考虑类型
                    sql.append("%s ".formatted(column.getType()));
                    // 类型考虑长度
                    if (!"".equals(column.getLength())) {
                        sql.append("(%s) ".formatted(column.getLength()));
                    }
                }
                //是否主键
                if (column.getIsPrimaryKey()) {
                    sql.append("PRIMARY KEY ");
                }
                //是否可以为空
                if (!column.getIsNullable()) {
                    sql.append("NOT NULL ");
                }
                //是否唯一
                if (column.getIsUnique()) {
                    sql.append("UNIQUE ");
                }
                //如果是主键，则默认值无效
                if (!column.getIsPrimaryKey() && !"".equals(column.getDefaultValue())) {
                    sql.append("DEFAULT '%s' ".formatted(column.getDefaultValue()));
                }
                //todo 外键等约束信息
                //要考虑表的创建顺序

                //每个字段结束，最后一个逗号在外部取消
                sql.append(",\n");
                //注释
                if (!"".equals(column.getComment())) {
                    comment.append("COMMENT ON COLUMN %s.%s IS '%s';\n".formatted(tableName, column.getName(), column.getComment()));
                }
            });
            //删除最后一个逗号
            sql.delete(sql.length() - 2, sql.length() - 1);
            sql.append(");\n");
            sql.append(comment);
        });
        return sql.toString();
    }


}
