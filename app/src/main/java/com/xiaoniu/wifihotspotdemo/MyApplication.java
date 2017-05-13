package com.xiaoniu.wifihotspotdemo;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

import org.xutils.x;

/**
 * Created by think on 2017/4/22 22:56.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        // 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
        // 设置你申请的应用appid
        SpeechUtility.createUtility(this, "appid=" + getString(R.string.app_id));

    }
}
