package com.lhlwork.enums.database;

import lombok.Getter;

@Getter
public enum ColumnTypeEnum {
    INT("INT"),
    SMALLINT("SMALLINT"),
    BIGINT("BIGINT"),
    BIT("BIT"),
    DECIMAL("DECIMAL"),
    CHAR("CHAR"),
    VARCHAR("VARCHAR"),
    DATE("DATE"),
    TIME("TIME"),
    POINT("POINT"),
    POLYGON("POLYGON"),
    OTHER("OTHER");


    ColumnTypeEnum(String type) {
        this.type = type;
    }

    private final String type;

}
