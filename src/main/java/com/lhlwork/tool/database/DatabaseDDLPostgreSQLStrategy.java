package com.lhlwork.tool.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component("org.postgresql.Driver")
@Slf4j
public class DatabaseDDLPostgreSQLStrategy implements DatabaseDDLStrategy {
    @Override
    public Boolean databaseExist(Statement statement, String database) throws SQLException {
        String query = "SELECT datname FROM pg_database WHERE datname = '" + database + "'";
        ResultSet resultSet = statement.executeQuery(query);
        boolean existed = resultSet.next();
        resultSet.close();
        return existed;
    }

    @Override
    public void createDatabase(Statement statement, String database) throws SQLException {
        String query = "CREATE DATABASE " + database;
        int executed = statement.executeUpdate(query);
        if (executed == 0) {
            log.info("create database {} success", database);
        }
    }

    @Override
    public void dropDatabase(Statement statement, String database) throws SQLException {
        String query = "DROP DATABASE " + database;
        int executed = statement.executeUpdate(query);
        if (executed == 0) {
            log.info("drop database {} success", database);
        }

    }

    @Override
    public void createTable(Statement statement, String database, String table) throws SQLException {
        String query = "CREATE TABLE " + database + "." + table + " (id SERIAL PRIMARY KEY)";
    }
}
