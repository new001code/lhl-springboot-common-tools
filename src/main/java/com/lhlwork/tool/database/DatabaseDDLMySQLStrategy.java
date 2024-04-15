package com.lhlwork.tool.database;

import com.lhlwork.anno.database.Table;
import com.lhlwork.config.ColumnProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("com.mysql.cj.jdbc.Driver")
@Slf4j
public class DatabaseDDLMySQLStrategy implements DatabaseDDLStrategy {
    @Override
    public Boolean databaseExist(Statement statement, String database) throws SQLException {
        String query = """
                SELECT COUNT(*) AS DatabaseExists
                FROM INFORMATION_SCHEMA.SCHEMATA
                WHERE SCHEMA_NAME = '%s';
                """.formatted(database);
        try (ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt("DatabaseExists") == 1;
            }
        }
        return false;
    }

    @Override
    public void createDatabase(Statement statement, String database) throws SQLException {
        String query = "CREATE DATABASE %s;".formatted(database);
        int executed = statement.executeUpdate(query);
        if (executed == 0) {
            log.info("create database {} success", database);
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
        currentTables.forEach((table, columnProperties) -> {
            String tableName = table.tableName();
            sql.append("CREATE TABLE IF NOT EXISTS %s(\n".formatted(tableName));
            columnProperties.forEach(column -> {
                sql.append("%s ".formatted(column.getName()));
                sql.append("%s".formatted(column.getType()));
                // 类型考虑长度
                if (!"".equals(column.getLength())) {
                    sql.append("(%s) ".formatted(column.getLength()));
                }
                // 是否自增
                if (column.getIsAutoIncrement()) {
                    sql.append(" AUTO_INCREMENT ");
                }
                //是否主键
                if (column.getIsPrimaryKey()) {
                    sql.append(" PRIMARY KEY ");
                }
                //是否可以为空
                if (!column.getIsNullable()) {
                    sql.append(" NOT NULL ");
                }
                //是否唯一
                if (column.getIsUnique()) {
                    sql.append(" UNIQUE ");
                }
                //如果是主键，则默认值无效
                if (!column.getIsPrimaryKey() && !"".equals(column.getDefaultValue())) {
                    sql.append(" DEFAULT '%s' ".formatted(column.getDefaultValue()));
                }
                //注释
                if (!"".equals(column.getComment())) {
                    sql.append(" COMMENT '%s'".formatted(column.getComment()));
                }
                if (Character.isWhitespace(sql.charAt(sql.length() - 1))) {
                    sql.deleteCharAt(sql.length() - 1);
                }
                //每个字段结束，最后一个逗号在外部取消
                sql.append(",\n");
            });
            //删除最后一个逗号
            sql.delete(sql.length() - 2, sql.length() - 1);
            sql.append(");");
        });
        return sql.toString();
    }

    @Override
    public Set<String> getTableNameSameSchema(Statement statement, String schemaName) throws SQLException {
        Set<String> set = new HashSet<>();
        String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE();";
        try (ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                set.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return set;
    }
}
