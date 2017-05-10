package com.xiaoniu.wifihotspotdemo;

import android.app.Application;

import org.xutils.x;

/**
 * Created by think on 2017/4/22 22:56.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
