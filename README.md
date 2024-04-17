# lhl-springboot-common-tools

A springboot3+,jdk21-based development kit.
一个基于springboot3+jdk21的通用工具包。

## 通用的web请求和响应格式

### ApiRequest

```java
private Integer page;
private Integer rows;

//......
//计算sql分页的limit和offset
public Integer getLimit() {
    this.limit = this.rows > 0 ? this.rows : 10;
    return this.limit;
}

public Integer getOffset() {
    this.offset = this.page > 0 ? (this.page - 1) * getLimit() : 0;
    return this.offset;
}
```

#### 示例

```java

@EqualsAndHashCode(callSuper = true)
@Data
public class EmailSendConfQueryParams extends ApiRequest {
    private String email;
    private String title;
    private String content;
    private String sendTime;
}
```

### ApiResponse

```java
public record ApiResponse(String code, String message, Object data, Boolean success) {
}

/*
 * 定义了 success,fail,error 三种状态
 * success: 状态码：1
 * fail: 状态码：0
 * error: 状态码：-1
 */
public static ApiResponse success(String message, Object data) {
    return new ApiResponse("1", message, data, true);
}
```

#### 示例

```java
    public ApiResponse querySendConf(@RequestBody EmailSendConfQueryParams params) {
    try {
        ResponsePage page = emailConfService.querySendConf(params);
        return ApiResponse.success("", page);
    } catch (ServiceException e) {
        return ApiResponse.fail(e.getMessage(), null);
    } catch (Exception e) {
        log.error("querySendConf error", e);
        return ApiResponse.error();
    }
}
```

## 基于springAop的方法执行时间记录器

在方法上使用注解`@TimeRecorder`即可记录该方法的执行时间，当然只能作用于`Bean`上的方法。
同时定义了执行时间、记录器、是否异步记录等参数以便可以适应不同的环境。
异步执行是通过定义了一个单例单线程的线程池去执行记录器任务。

```java
/**
 * 执行时间 > targetTime 时，才进行记录。
 */
long targetTime() default 0;

/**
 * 定义进行记录的记录器
 */
Class<? extends TimeRecorderInterface> recorder() default TimeRecorderInterfaceLog .class;

/**
 * 是否异步记录
 */
boolean async() default true;
```

默认的记录器是`TimeRecorderInterfaceLog`，可以自定义实现`TimeRecorderInterface`接口，实现自己的记录器。

```java

@Slf4j
public class TimeRecorderInterfaceLog implements TimeRecorderInterface {
    @Override
    public void record(String name, long time) {
        log.info("Method:{}, Time:{}ms", name, time);
    }
}
```

#### 示例

```java

@TimeRecorder(async = false)
public ResponsePage querySendConf(EmailSendConfQueryParams params) throws ServiceException {
    List<Map<String, Object>> list = emailConfMapper.querySendConf(params);
    //...其他耗时操作
    return ResponsePage;
}
```

## 参数验证工具

`AssertThrowExceptionUtil`适用于各种需要参数验证，不通过则抛出异常的场景。使用回调函数传入验证逻辑，比较优雅地避免大量的if判断。

```java
// 传入函数（验证的逻辑），参数，异常，验证不通过时抛出异常    
public static <F, E extends Throwable> void assertThrowException(Function<F, Boolean> function, F param, E exception) throws E {
    if (!function.apply(param)) {
        throw exception;
    }
}

public static <F, G, E extends Throwable> void assertThrowException(BiFunction<F, G, Boolean> function, F param, G param2, String message, E exception) throws E {
    if (!function.apply(param, param2)) {
        throw exception;
    }
}
//......
```

### 示例

```java
AssertThrowExceptionUtil
        .getInstance()
        .multiAssertThrowException(StringUtil::isNotEmpty, map.get("driver-class-name"), new DatabasePropertiesBindException("driver-class-name is null"))
        .multiAssertThrowException(StringUtil::isNotEmpty, map.get("database"), new DatabasePropertiesBindException("database is null"))
        .multiAssertThrowException(StringUtil::isNotEmpty, map.get("url"), new DatabasePropertiesBindException("url is null"))
        .multiAssertThrowException(StringUtil::isNotEmpty, map.get("username"), new DatabasePropertiesBindException("username is null"));
```

## DatabaseActuator

此工具包提供了数据库DDL的部分操作，相较于现有的DDL工具，此工具包提供的DDL操作主要有以下几个特点：

1. 使用简单，只需引入此数据包，在应用的配置文件中配置数据库连接等信息，即可使用。
2. 提供了通过编码和sql脚本两种的方式。
3. 支持多种数据库，可以同时对多个数据库进行操作。
4. 提供了异步执行功能，可以异步执行DDL操作。
5. 提供了日志功能，可以记录DDL操作的sql语句。
6. 提供了数据库的扩展接口，可以接入自己的数据库操作。

### 使用方式

目前还没有上传到maven中央仓库，所以需要下载源码，编译成jar包，然后手动引入。

1. github仓库地址

   [lhl-springboot-common-tools](https://github.com/new001code/lhl-springboot-common-tools)
2. 引入
* maven
 ```xml 
<dependency>
   <groupId>com.lhlwork</groupId>
   <artifactId>lhl-springboot-common-tools</artifactId>
   <version>1.0</version>
</dependency>
```  
* gradle
```groovy
dependencies {
  compile 'com.lhlwork:lhl-springboot-common-tools:1.0'
}
```
3. 配置

```yaml
database-generate: # 数据库DDL生成配置
  table-locations: com.lhlwork.mail.table # 扫描的包，包括其子包
  async: true # 是否异步执行，默认为false
  list: # 多个数据库配置
    - driver-class-name: org.postgresql.Driver # 数据库驱动，必填
      url: jdbc:postgresql://localhost:5432/ # 数据库连接地址，必填
      database: company_test # 数据库名称，必填
      username: postgres_user # 数据库用户名，必填
      password: postgres_password # 数据库密码
      execute-type: file # 执行类型，file为文件执行，update为sql执行，并且update只会去执行当前数据库中还没有的表的DDL。
      file: /home/user/Code/java/test.sql # 文件执行时，文件路径
    - driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/
      database: company_test
      username: mysql-user
      password: mysql-password
      execute-type: update

logging:
   pattern:
      console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}[%L] - %msg%n'
   level:
      com.lhlwork: debug #开启这个包的debug日志级别，可以看到生成的sql。
```

4. 定义数据表

`@Table`中的属性都是必填的，要和配置文件中的对应的数据库配置一致。
`@Column`中是关于数据库字段的配置，可以不填，但是不填的话，数据库中不会生成该字段。
如果内置的类型无法满足，可以选择`ColumnTypeEnum.OTHER`，然后在`otherColumnType`中填写对应的数据类型。

```java
@Table(tableName = "table_test", url = "jdbc:mysql://localhost:3306/", username = "root", database = "company_test")
public class TableTest {
   @Column(isPrimaryKey = true, isAutoIncrement = true, isNullable = false, isUnique = true, type = ColumnTypeEnum.BIGINT)
   private Long id;
   @Column(type = ColumnTypeEnum.OTHER, length = "200", otherColumnType = "varchar", isNullable = false, isUnique = true, defaultValue = "hello", comment = "hello world")
   private String hostName;

   @Column(type = ColumnTypeEnum.DECIMAL, length = "10,2")
   private BigDecimal whatName;

   @Column(type = ColumnTypeEnum.DATE)
   private Date createTime;
}
```

5. 其他数据库的扩展

包中只实现了常用的`MYSQL`和`POSTGRESQL`两种类型的数据库，如果需要其他类型的数据库，可以自行实现`DatabaseDDLStrategy`接口，同时这里使用了基于springboot的策略模式，因此需要将实现的类注册为spring bean,名称为数据库的驱动。

```java

@Component("org.postgresql.Driver")
@Slf4j
public class DatabaseDDLPostgreSQLStrategy implements DatabaseDDLStrategy {
    @Override
    public Boolean databaseExist(Statement statement, String database) throws SQLException {
        String query = "SELECT datname FROM pg_database WHERE datname = '%s';".formatted(database);
        try (ResultSet resultSet = statement.executeQuery(query)) {
            return resultSet.next();
        }
    }
    //......
}
```

6. 运行
该功能会在项目启动时自动执行，因此只需要配置好前面的信息，然后启动项目即可。
```
2024-04-15 16:38:14.803 [main] INFO  c.l.t.d.DatabaseConnectFactory[43] - database connection success:com.mysql.cj.jdbc.Driver-jdbc:mysql://localhost:3306/-root
2024-04-15 16:38:14.806 [main] INFO  c.l.t.database.DatabaseDDLActuator[71] - A table was scanned:com.lhlwork.mail.table.MailConf
2024-04-15 16:38:14.807 [main] INFO  c.l.t.database.DatabaseDDLActuator[71] - A table was scanned:com.lhlwork.mail.table.test.TableTest
2024-04-15 16:38:14.813 [DataActuatorThread] INFO  c.l.t.database.DatabaseDDLActuator[147] - the database does not exist, create the database：company_test
2024-04-15 16:38:14.829 [DataActuatorThread] DEBUG c.l.t.d.DatabaseDDLMySQLStrategy[46] - CREATE TABLE IF NOT EXISTS table_test(
id BIGINT AUTO_INCREMENT  PRIMARY KEY  NOT NULL  UNIQUE,
host_name varchar(200)  NOT NULL  UNIQUE  DEFAULT 'hello'  COMMENT 'hello world',
what_name DECIMAL(10,2),
create_time DATE
);
2024-04-15 16:38:14.873 [DataActuatorThread] INFO  c.l.t.d.DatabaseDDLMySQLStrategy[49] - create tables success
```


