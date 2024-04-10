# lhl-springboot-common-tools
A springboot3+,jdk21-based development kit.
一个基于springboot3+jdk21的通用工具包。
## 通用的web请求和响应格式
### ApiRequest
```java
private Integer page;
private Integer rows;
//......
```
### ApiResponse
```java
public record ApiResponse(String code, String message, Object data, Boolean success){}
//......
```
## 基于springAop的方法执行时间记录器
在方法上使用注解`@TimeRecorder`即可记录该方法的执行时间，当然只能作用于`Bean`上的方法。
```java
    /**
     * 执行时间 > targetTime 时，才进行记录。
     */
    long targetTime() default 0;

    /**
     * 定义进行记录的记录器
     */
    Class<? extends TimeRecorderInterface> recorder() default TimeRecorderInterfaceLog.class;

    /**
     * 是否异步记录
     */
    boolean async() default true;
```
## 参数验证工具
`AssertThrowExceptionUtil`
```java
// 传入函数（验证的逻辑），参数，异常，验证不通过时抛出异常    
public static <F, E extends Throwable> void assertThrowException(Function<F, Boolean> function, F param, E exception) throws E {
    if (!function.apply(param)) {
        throw exception;
    }
}
//......
```