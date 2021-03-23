package com.bdxh.mqttconnection.download;

public interface ProgressListener {
    void update(String url, long bytesRead, long contentLength, boolean done);
}