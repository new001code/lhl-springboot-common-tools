package com.lhlwork.anno.database;

import com.lhlwork.enums.database.ColumnTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 列名: 默认使用属性的下划线命名，如果特别指定则使用name
     * @return name
     */
    String name() default "";
    /**
     * 是否为主键
     * @return isPrimaryKey
     */
    boolean isPrimaryKey() default false;
    /**
     * 外键
     * @return foreignKey
     */
    String foreignKey() default "";
    /**
     * 是否自增
     * @return isAutoIncrement
     */
    boolean isAutoIncrement() default false;
    /**
     * 是否可以为空
     * @return isNullable
     */
    boolean isNullable() default true;
    /**
     * 是否唯一
     * @return isUnique
     */
    boolean isUnique() default false;
    /**
     * 默认值
     * @return defaultValue
     */
    String defaultValue() default "";
    /**
     * 注释
     * @return comment
     */
    String comment() default "";
    /**
     * 类型
     * @return type
     */
    ColumnTypeEnum type();
    /**
     * 其他类型
     * @return otherColumnType
     */
    String otherColumnType() default "";
    /**
     * 长度
     * @return length
     */
    String length() default "";

}
