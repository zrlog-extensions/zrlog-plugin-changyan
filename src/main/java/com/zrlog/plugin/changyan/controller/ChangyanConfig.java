package com.zrlog.plugin.changyan.controller;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ChangyanConfig {

    private String appId;
    private String appKey;
    private String status;
    private String commentEmailNotify;
    private String callbackUrl;
    @SerializedName("short_name")
    private String shortName;
    private String secret;
    private String version;

    public void normalize(String callbackUrl, String version) {
        if (isBlank(this.callbackUrl)) {
            this.callbackUrl = callbackUrl;
        }
        status = switchValue(status);
        commentEmailNotify = switchValue(commentEmailNotify);
        this.version = version;
    }

    public boolean isCommentEmailNotifyEnabled() {
        return Objects.equals(commentEmailNotify, "on");
    }

    private String switchValue(String value) {
        if (Objects.equals(value, "on")) {
            return "on";
        }
        return "off";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommentEmailNotify() {
        return commentEmailNotify;
    }

    public void setCommentEmailNotify(String commentEmailNotify) {
        this.commentEmailNotify = commentEmailNotify;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
