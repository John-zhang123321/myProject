package com.zl.authority.entity;

/**
 * Created by zhangliang on 2018/9/1.
 */
public class ResultObject<T> {
    private int responseCode;
    private T data;
    private String msg;


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
