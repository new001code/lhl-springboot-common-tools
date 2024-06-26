package com.lhlwork.enums.database;

import lombok.Getter;

@Getter
public enum ExecuteTypeEnum {
    FILE("FILE"),
    UPDATE("UPDATE");


    private final String type;

    ExecuteTypeEnum(String type) {
        this.type = type;
    }
}
