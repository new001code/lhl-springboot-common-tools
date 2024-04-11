package com.lhlwork.tool.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
//@ConfigurationProperties(prefix = "database-generate")
@Data
public class DatabaseConnectFactory {
    private List<Map<String, String>> list;
}
