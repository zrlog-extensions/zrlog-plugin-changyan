package com.zrlog.plugin.changyan.controller;

public class ChangyanApiResponse<T> {

    private boolean success;
    private T data;

    public ChangyanApiResponse() {
    }

    private ChangyanApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public static ChangyanApiResponse<Void> success() {
        return new ChangyanApiResponse<Void>(true, null);
    }

    public static <T> ChangyanApiResponse<T> success(T data) {
        return new ChangyanApiResponse<T>(true, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
