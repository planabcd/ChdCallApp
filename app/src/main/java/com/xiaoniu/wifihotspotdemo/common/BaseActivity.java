package com.xiaoniu.wifihotspotdemo.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by think on 2017/5/13 9:57.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
