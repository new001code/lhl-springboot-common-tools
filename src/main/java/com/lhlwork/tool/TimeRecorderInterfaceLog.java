package com.lhlwork.tool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeRecorderInterfaceLog implements TimeRecorderInterface {
    @Override
    public void record(String name, long time) {
        log.info("Method:{}, Time:{}ms", name, time);
    }
}
