package com.lhlwork.config;

import com.lhlwork.enums.database.ExecuteTypeEnum;
import com.lhlwork.exception.database.DatabasePropertiesBindException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "database-generate")
@Data
@Component
public class DatabaseGenerateProperties{

    private final List<Map<String, String>> list;



    public List<DatabaseGenerateConfig> getConfigList() throws DatabasePropertiesBindException {
        try {
            return list.stream().map(map -> new DatabaseGenerateConfig(
                    map.get("driver-class-name"),
                    map.get("url"),
                    map.get("username"),
                    map.get("password"),
                    ExecuteTypeEnum.valueOf(map.get("execute-type").toUpperCase()),
                    map.get("database")
            )).toList();
        } catch (Exception e) {
            throw new DatabasePropertiesBindException(e);
        }

    }

    public record DatabaseGenerateConfig(String driverClassName, String url, String username, String password,
                                         ExecuteTypeEnum executeType, String database) {
    }


}
