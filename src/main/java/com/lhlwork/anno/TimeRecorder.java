package com.lhlwork.anno;

import com.lhlwork.tool.TimeRecorderInterface;
import com.lhlwork.tool.TimeRecorderInterfaceLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 记录方法执行时间
 */
@Target({ElementType.METHOD})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface TimeRecorder {

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
}
