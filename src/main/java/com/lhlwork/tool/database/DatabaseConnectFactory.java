package com.lhlwork.tool.database;

import com.lhlwork.config.DatabaseGenerateProperties;
import com.lhlwork.exception.database.DatabaseConnectInitException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@DependsOn("databaseGenerateProperties")
public class DatabaseConnectFactory {

    @Getter
    private final Map<String, Connection> connectionMap = new HashMap<>();
    @Resource
    private DatabaseGenerateProperties properties;


    @PostConstruct
    public void getDatabaseConnect() {
        if (properties != null && properties.getConfigList() != null && !properties.getConfigList().isEmpty()) {
            // 根据数据库配置生成数据库连接
            List<DatabaseGenerateProperties.DatabaseGenerateConfig> configList = properties.getConfigList();

            for (DatabaseGenerateProperties.DatabaseGenerateConfig config : configList) {
                //获取名称等的唯一标识，一个数据库,同一个用户只创建一个连接
                String key = config.driverClassName() + "-" + config.url() + "-" + config.username();
                Connection connect = this.getDatabaseConnect(config);
                if (connect == null) {
                    log.error("database connection error,please check Driver、url、username and database");
                }
                if (connect != null && !this.connectionMap.containsKey(key)) {
                    log.info("database connection success:{}", key);
                    this.connectionMap.put(key, connect);
                }
            }
        }
    }

    private Connection getDatabaseConnect(DatabaseGenerateProperties.DatabaseGenerateConfig config) {
        Connection connection;
        try {
            //注册数据库驱动
            Class.forName(config.driverClassName());
            connection = DriverManager.getConnection(config.url(), config.username(), config.password());
        } catch (Exception e) {
            throw new DatabaseConnectInitException(e);
        }
        return connection;
    }


}
