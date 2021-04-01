package com.bdxh.mqttconnection.detach;

import android.content.res.Resources;

/**
 *  资源封装
 */
public class LoadResourcesBean {

    private Resources resources ;

    private String packageName;

    private ClassLoader classLoader;

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
