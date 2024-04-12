package com.lhlwork.tool.database;

import com.lhlwork.anno.database.Column;
import com.lhlwork.anno.database.Table;
import com.lhlwork.config.ColumnProperties;
import com.lhlwork.config.DatabaseGenerateProperties;
import com.lhlwork.exception.database.DatabaseConnectInitException;
import com.lhlwork.exception.database.DatabaseDriverStrategyException;
import com.lhlwork.exception.database.TableException;
import com.lhlwork.exception.database.TableNotFoundException;
import com.lhlwork.tool.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@DependsOn("databaseConnectFactory")
@Slf4j
public class DatabaseDDLActuator {

    @Resource
    private DatabaseGenerateProperties properties;
    @Resource
    private DatabaseConnectFactory databaseConnectFactory;
    @Resource
    private Map<String, DatabaseDDLStrategy> databaseDDLStrategyMap;

    @PostConstruct
    public void execute() {
        Map<String, Connection> connectionMap = databaseConnectFactory.getConnectionMap();
        if (connectionMap.isEmpty()) {
            return;
        }
//        int size = connectionMap.size();
//        CountDownLatch countDownLatch = new CountDownLatch(size);
        Map<String, DatabaseGenerateProperties.DatabaseGenerateConfig> configMap = properties.getConfigList().stream().collect(Collectors.toMap(c -> c.driverClassName() + "-" + c.url() + "-" + c.username(), Function.identity()));
        Map<Table, List<ColumnProperties>> tableList = importTable(properties.getTableLocations());
        databaseExecute(configMap, connectionMap);
    }

    private Map<Table, List<ColumnProperties>> importTable(String tableLocations) {
        //Class Scanner
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Table.class));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(tableLocations);
        Set<String> tableSet = new HashSet<>();
        Map<Table, List<ColumnProperties>> result = new HashMap<>(16);
        for (BeanDefinition candidateComponent : candidateComponents) {
            String className = candidateComponent.getBeanClassName();
            log.info("A table was scanned:{}", className);
            try {
                Class<?> clazz = Class.forName(className);
                Table annotation = clazz.getAnnotation(Table.class);
                String key = annotation.tableName() + "-" + annotation.url() + "-" + annotation.username() + "-" + annotation.database();
                if (tableSet.contains(key)) {
                    throw new TableException("The table name is repeated," + annotation.tableName());
                } else {
                    tableSet.add(key);
                }
                Field[] declaredFields = clazz.getDeclaredFields();
                List<ColumnProperties> columnPropertiesList = new ArrayList<>();
                boolean hasPrimaryKey = false;
                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    Column column = declaredField.getAnnotation(Column.class);
                    if (hasPrimaryKey && column.isPrimaryKey()) {
                        throw new TableException("The table has more than one primary key," + tableLocations + "." + clazz.getName());
                    }
                    if (column.isPrimaryKey()) {
                        hasPrimaryKey = true;
                    }
                    String type = column.type().getType().equals("OTHER") ? column.otherColumnType() : column.type().getType();
                    //字段名，当name没有设置，则使用该字段名
                    String name = declaredField.getName();
                    if ("".equals(type)) {
                        throw new TableException("The column type Unknown," + tableLocations + "." + clazz.getName() + "." + name);
                    }
                    ColumnProperties build = ColumnProperties.builder()
                            .name("".equals(column.name()) ? StringUtil.camelCaseToUnderscore(name) : column.name())
                            .isPrimaryKey(column.isPrimaryKey())
                            .foreignKey(column.foreignKey())
                            .isAutoIncrement(column.isAutoIncrement())
                            .isNullable(column.isNullable())
                            .isUnique(column.isUnique())
                            .defaultValue(column.defaultValue())
                            .comment(column.comment())
                            .type(type)
                            .length(column.length())
                            .build();
                    columnPropertiesList.add(build);
                }
                result.put(annotation, columnPropertiesList);

            } catch (ClassNotFoundException e) {
                throw new TableNotFoundException(e);
            }
        }
        return result;

    }

    /**
     * 1.按驱动，连接的url和用户名进行分组，每一组分别执行(公用一个数据库连接)。
     * 2.每一组中可能会有不同的数据库类型，所以需要执行不同的策略。
     */
    private void databaseExecute(Map<String, DatabaseGenerateProperties.DatabaseGenerateConfig> configMap, Map<String, Connection> connectionMap) {
        for (Map.Entry<String, DatabaseGenerateProperties.DatabaseGenerateConfig> entry : configMap.entrySet()) {
            String key = entry.getKey();
            DatabaseGenerateProperties.DatabaseGenerateConfig config = entry.getValue();
            if (databaseDDLStrategyMap.containsKey(config.driverClassName())) {
                DatabaseDDLStrategy databaseDDLStrategy = databaseDDLStrategyMap.get(config.driverClassName());
                //获取数据库连接
                Connection connection = connectionMap.get(key);
                try {
                    Statement statement = connection.createStatement();
                    //判断当前数据库是否存在
                    Boolean databasedExist = databaseDDLStrategy.databaseExist(statement, config.database());
                    if (!databasedExist) {
                        log.info("the database does not exist, create the database：{}", config.database());
                        //创建数据库
                        databaseDDLStrategy.createDatabase(statement, config.database());
                    } else {
                        log.info("the database already exists：{}", config.database());
                        /*
                        根据策略：
                        force：强制删除数据库，然后重新创建数据库；
                         */
                        switch (config.executeType()) {
                            case FORCE:
                                log.info("drop the database：{}", config.database());
                                //删除数据库
                                databaseDDLStrategy.dropDatabase(statement, config.database());
                                //创建数据库
                                databaseDDLStrategy.createDatabase(statement, config.database());
                            case null:
                                //默认不进行任何操作
                        }
                    }
                } catch (SQLException e) {
                    throw new DatabaseConnectInitException(e);
                }
            } else {
                throw new DatabaseDriverStrategyException("A Bean named " + config.driverClassName() + " could not be found. Check that the configuration is correct or that the interface `DatabaseDDLStrategy` is implemented!");
            }

        }
    }
}
