package com.lhlwork.config;

import com.lhlwork.enums.database.ExecuteTypeEnum;
import com.lhlwork.exception.database.DatabasePropertiesBindException;
import com.lhlwork.tool.AssertThrowExceptionUtil;
import com.lhlwork.tool.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "database-generate")
@Setter
@Getter
@Component
public class DatabaseGenerateProperties {
    private String tableLocations;
    private Boolean async = false;
    private List<Map<String, String>> list;

    private void checkMap(List<Map<String, String>> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        /*
          1.校验：driver-class-name, url, username,database 为必填字段。
          2.校验：如果类型是file，则校验file地址不能为空。
          3. database不传，则必须使用file，否则报错
         */
        list.forEach(map -> AssertThrowExceptionUtil.getInstance().multiAssertThrowException(StringUtil::isNotEmpty, map.get("driver-class-name"), new DatabasePropertiesBindException("driver-class-name is null"))
                .multiAssertThrowException(StringUtil::isNotEmpty, map.get("database"), new DatabasePropertiesBindException("database is null"))
                .multiAssertThrowException(StringUtil::isNotEmpty, map.get("url"), new DatabasePropertiesBindException("url is null"))
                .multiAssertThrowException(StringUtil::isNotEmpty, map.get("username"), new DatabasePropertiesBindException("username is null"))
                .multiAssertThrowException((String t, String f) -> {
                    if (StringUtil.isNotEmpty(t) && ExecuteTypeEnum.valueOf(t.toUpperCase()) == ExecuteTypeEnum.FILE) {
                        return StringUtil.isNotEmpty(f);
                    }
                    return true;
                }, map.get("execute-type"), map.get("file"), new DatabasePropertiesBindException("when execute-type is FILE, column file is required")));
    }


    public List<DatabaseGenerateConfig> getConfigList() throws DatabasePropertiesBindException {
        checkMap(list);
        try {
            return list.stream().map(map -> new DatabaseGenerateConfig(map.get("driver-class-name"), map.get("url"), map.get("username"), map.get("password"), ExecuteTypeEnum.valueOf(StringUtil.isNotEmpty(map.get("execute-type")) ? map.get("execute-type").toUpperCase() : ExecuteTypeEnum.UPDATE.name()), map.get("database"), map.get("file"))).toList();
        } catch (Exception e) {
            throw new DatabasePropertiesBindException(e);
        }

    }

    public record DatabaseGenerateConfig(String driverClassName, String url, String username,
                                         String password, ExecuteTypeEnum executeType,
                                         String database, String file) {
    }


}
