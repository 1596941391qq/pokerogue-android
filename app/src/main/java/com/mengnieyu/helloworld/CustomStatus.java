package com.mengnieyu.helloworld;

import org.nanohttpd.protocols.http.response.IStatus;

public class CustomStatus implements IStatus {
    protected int code;
    protected String description;

    public CustomStatus(int code, String descriptcion) {
        this.code = code;
        this.description = description;
    }
    @Override
    public int getRequestStatus() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
