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
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
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

    @Getter
    static class DatabaseActuatorThreadPool {
        static class DatabaseActuatorThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread t = new Thread(r);
                t.setName("DatabaseActuator");
                t.setDaemon(true);
                return t;
            }
        }

        // 使用volatile关键字保证多线程间的可见性
        private volatile static DatabaseDDLActuator.DatabaseActuatorThreadPool instance;
        // 提供获取线程池的方法
        private final ExecutorService threadPool;

        // 私有化构造函数
        private DatabaseActuatorThreadPool() {
            threadPool = new ThreadPoolExecutor(1, 3, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new DatabaseDDLActuator.DatabaseActuatorThreadPool.DatabaseActuatorThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());

        }

        // 提供获取实例的方法
        public static DatabaseActuatorThreadPool getInstance() {
            if (instance == null) {
                synchronized (DatabaseActuatorThreadPool.class) {
                    if (instance == null) {
                        instance = new DatabaseActuatorThreadPool();
                    }
                }
            }
            return instance;
        }

    }

    @PostConstruct
    public void execute() {
        Map<String, Connection> connectionMap = databaseConnectFactory.getConnectionMap();
        if (connectionMap.isEmpty()) {
            return;
        }
        //按驱动-url-用户分组,可能有不同的数据库
        Map<String, List<DatabaseGenerateProperties.DatabaseGenerateConfig>> configMap = properties.getConfigList().stream().collect(Collectors.toMap(c -> "%s-%s-%s".formatted(c.driverClassName(), c.url(), c.username()), List::of, (a, b) -> {
            List<DatabaseGenerateProperties.DatabaseGenerateConfig> list = new ArrayList<>(a);
            list.addAll(b);
            return list;
        }));
        Map<Table, List<ColumnProperties>> tableList = importTable(properties.getTableLocations());
        Boolean async = properties.getAsync();
        databaseExecute(configMap, connectionMap, tableList, async);
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
                String key = "%s-%s-%s-%s".formatted(annotation.tableName(), annotation.url(), annotation.username(), annotation.database());
                if (tableSet.contains(key)) {
                    throw new TableException("The table name is repeated,%s".formatted(annotation.tableName()));
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
                        throw new TableException("The table has more than one primary key,%s.%s".formatted(tableLocations, clazz.getName()));
                    }
                    if (column.isPrimaryKey()) {
                        hasPrimaryKey = true;
                    }
                    String type = column.type().getType().equals("OTHER") ? column.otherColumnType() : column.type().getType();
                    //字段名，当name没有设置，则使用该字段名
                    String name = declaredField.getName();
                    if ("".equals(type)) {
                        throw new TableException("The column type Unknown,%s.%s.%s".formatted(tableLocations, clazz.getName(), name));
                    }
                    columnPropertiesList.add(getColumnProperties(column, name, type));
                }
                result.put(annotation, columnPropertiesList);

            } catch (ClassNotFoundException e) {
                throw new TableNotFoundException(e);
            }
        }
        return result;

    }

    private static ColumnProperties getColumnProperties(Column column, String name, String type) {
        return ColumnProperties.builder().name("".equals(column.name()) ? StringUtil.camelCaseToUnderscore(name) : column.name()).isPrimaryKey(column.isPrimaryKey()).foreignKey(column.foreignKey()).isAutoIncrement(column.isAutoIncrement()).isNullable(column.isNullable()).isUnique(column.isUnique()).defaultValue(column.defaultValue()).comment(column.comment()).type(type).length(column.length()).build();
    }

    /**
     * 1.按驱动，连接的url和用户名进行分组，每一组分别执行(公用一个数据库连接)。
     * 2.每一组中可能会有不同的数据库类型（不同的驱动），所以需要执行不同的策略。
     * 3.按url和用户名进行分组，在不同（1中的数据库连接中）连接中执行DDL。
     */
    private void databaseExecute(Map<String, List<DatabaseGenerateProperties.DatabaseGenerateConfig>> configMap, Map<String, Connection> connectionMap, Map<Table, List<ColumnProperties>> tableList, Boolean async) {
        for (Map.Entry<String, List<DatabaseGenerateProperties.DatabaseGenerateConfig>> entry : configMap.entrySet()) {
            if (async) {
                DatabaseActuatorThreadPool.getInstance().getThreadPool().execute(() -> extracted(connectionMap, tableList, entry));
            } else {
                extracted(connectionMap, tableList, entry);
            }
        }
    }

    private void extracted(Map<String, Connection> connectionMap, Map<Table, List<ColumnProperties>> tableList, Map.Entry<String, List<DatabaseGenerateProperties.DatabaseGenerateConfig>> entry) {
        String key = entry.getKey();
        List<DatabaseGenerateProperties.DatabaseGenerateConfig> configList = entry.getValue();
        //同一个驱动、url、用户名，执行数据库操作使用同一个连接。
        //获取数据库连接，此时的连接是public，可以执行创建数据库，删除数据库的等操作。
        //一个针对{数据库}的Statement,
        try (Connection connection = connectionMap.get(key); Statement statement = connection.createStatement()) {
            Statement tableStatement = null;
            //连接同一个数据库的实例，可能会创建多个database。
            for (DatabaseGenerateProperties.DatabaseGenerateConfig config : configList) {
                if (databaseDDLStrategyMap.containsKey(config.driverClassName())) {
                    DatabaseDDLStrategy databaseDDLStrategy = databaseDDLStrategyMap.get(config.driverClassName());
                    //判断当前数据库是否存在
                    boolean databasedExist = databaseDDLStrategy.databaseExist(statement, config.database());
                    if (!databasedExist) {
                        log.info("the database does not exist, create the database：{}", config.database());
                        //创建数据库
                        databaseDDLStrategy.createDatabase(statement, config.database());
                        tableStatement = getDatabaseStatement(config);
                        String sql = getSqlFromFile(config.file());
                        databaseDDLStrategy.executeByFile(tableStatement, sql);
                    } else {
                        switch (config.executeType()) {
                            case FILE:
                                //file：执行sql文件,如果类型是文件，那么就只执行sql文件，不会执行代码的内容。
                                tableStatement = getDatabaseStatement(config);
                                String sql = getSqlFromFile(config.file());
                                databaseDDLStrategy.executeByFile(tableStatement, sql);
                                break;
                            case UPDATE:
                                tableStatement = getDatabaseStatement(config);
                                //获取该数据库连接下，可能需要执行的表
                                Set<String> tableSet = databaseDDLStrategy.getTableNameSameSchema(tableStatement, "public");
                                Map<Table, List<ColumnProperties>> currentTables = getCurrentTables(tableList, config, tableSet);
                                if (!currentTables.isEmpty()) {
                                    databaseDDLStrategy.createTables(tableStatement, currentTables);
                                }
                                break;
                        }
                    }
                    if (tableStatement != null) {
                        tableStatement.close();
                        tableStatement.getConnection().close();
                    }

                } else {
                    throw new DatabaseDriverStrategyException("A Bean named %s could not be found. Check that the configuration is correct or that the interface `DatabaseDDLStrategy` is implemented!".formatted(config.driverClassName()));
                }
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseConnectInitException(e);
        }
    }

    /**
     * 获取新的数据库连接，此时绑定到具体的database
     */
    private static Statement getDatabaseStatement(DatabaseGenerateProperties.DatabaseGenerateConfig config) throws SQLException {
        Connection tableConnection = DriverManager.getConnection(config.url() + config.database(), config.username(), config.password());
        return tableConnection.createStatement();
    }

    private static Map<Table, List<ColumnProperties>> getCurrentTables(Map<Table, List<ColumnProperties>> tableList, DatabaseGenerateProperties.DatabaseGenerateConfig config, Set<String> tableSet) {
        return tableList.entrySet()
                .stream()
                .filter(table -> !tableSet.contains(table.getKey().tableName()))
                .filter(table -> table.getKey().url().equals(config.url()) && table.getKey().username().equals(config.username())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String getSqlFromFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();

    }
}
