package com.xiaoniu.wifihotspotdemo.common;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2017/5/13 9:56
 * Activity管理类
 */

public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

}