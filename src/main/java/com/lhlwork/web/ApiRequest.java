package com.lhlwork.web;

import java.io.Serial;
import java.io.Serializable;

public class ApiRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 18908908908908L;

    private Integer page;

    private Integer rows;

    private String keyword;

    private String sort;

    private String order;

    private String type;

    private String status;

    private String startTime;

    private String endTime;

    private Integer limit;

    private Integer offset;

    public Integer getLimit() {
        if (this.rows == null) {
            this.limit = 10;
        } else {
            this.limit = this.rows > 0 ? this.rows : 10;
        }
        return this.limit;
    }

    public Integer getOffset() {
        if (this.page == null) {
            this.offset = 0;
        } else {
            this.offset = this.page > 0 ? (this.page - 1) * getLimit() : 0;
        }
        return this.offset;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
