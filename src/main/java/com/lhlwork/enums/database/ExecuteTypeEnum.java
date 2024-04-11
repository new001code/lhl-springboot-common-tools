package com.lhlwork.enums.database;

import lombok.Getter;

@Getter
public enum ExecuteTypeEnum {
    FORCE("FORCE");


    private final String type;

    ExecuteTypeEnum(String type) {
        this.type = type;
    }
}
