package com.exampl.smartcourse.common;

public class PageResult {
    private int code;
    private String message;
    private long total;
    private Object data;

    public PageResult() {
    }

    public PageResult(int code, String message, long total, Object data) {
        this.code = code;
        this.message = message;
        this.total = total;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}