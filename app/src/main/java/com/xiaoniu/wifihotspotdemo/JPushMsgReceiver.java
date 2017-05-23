package com.xiaoniu.wifihotspotdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by think on 2017/5/23 14:11.
 */

public class JPushMsgReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(TextUtils.isEmpty(action)){
            UIUtil.showToastS(context,"获取推送广播失败");
            return;
        }
        if(action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)){
            Bundle bundle = intent.getExtras();
            String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
            String content = bundle.getString(JPushInterface.EXTRA_ALERT);
            String type = bundle.getString(JPushInterface.EXTRA_EXTRA);
            UIUtil.showToast(context,title+"-"+content+"-"+type);
        }
    }
}
