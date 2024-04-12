package com.lhlwork.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnProperties {

    private String name;

    private Boolean isPrimaryKey;

    private String foreignKey;

    private Boolean isAutoIncrement;

    private Boolean isNullable;

    private Boolean isUnique;

    private String defaultValue;

    private String comment;

    private String type;

    private String length;


}
