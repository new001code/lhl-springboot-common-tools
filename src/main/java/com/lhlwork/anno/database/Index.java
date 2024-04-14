package com.lhlwork.anno.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Index {
    String name() default "";

    /*
    组合索引按这个分组
     */
    String group() default "";

    /*
    组合索引时确定索引的顺序
     */
    int Order() default 0;
}
