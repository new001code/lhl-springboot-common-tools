package com.lhlwork.tool.database;

import com.lhlwork.config.DatabaseGenerateProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

@Component
@Slf4j
@DependsOn("databaseGenerateProperties")
public class DatabaseConnectFactory {
    @Resource
    private DatabaseGenerateProperties properties;

    @PostConstruct
    public Map<String, Object> getDatabaseConnect() {
        if (properties != null && !properties.getConfigList().isEmpty()) {
            // 根据数据库配置生成数据库连接
            properties.getConfigList().forEach(config -> {
                Connection connect = this.getDatabaseConnect(config);
                if (connect != null) {
                    log.info("数据库连接成功");
                }
                log.info("{}",connect);
            });

        }
        return null;
    }

    private Connection getDatabaseConnect(DatabaseGenerateProperties.DatabaseGenerateConfig config) {
        Connection connection = null;
        try {
            //注册数据库驱动
            Class.forName(config.driverClassName());
            connection = DriverManager.getConnection(config.url(), config.username(), config.password());
        } catch (Exception e) {
            log.error("数据库连接失败", e);
        }


        return connection;
    }


}
