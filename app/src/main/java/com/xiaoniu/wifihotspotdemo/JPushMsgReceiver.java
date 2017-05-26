package com.xiaoniu.wifihotspotdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.Map;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by think on 2017/5/23 14:11.
 */

public class JPushMsgReceiver extends BroadcastReceiver {
    private LocationManager mLocationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(TextUtils.isEmpty(action)){
            UIUtil.showToastS(context,"获取推送广播失败");
            return;
        }
        if(action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)){
            Bundle bundle = intent.getExtras();
            String extraJson = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if(TextUtils.isEmpty(extraJson) || extraJson.length()<3){
                UIUtil.showToastS(context,"未获取到推送信息");
                return;
            }
            String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
            String content = bundle.getString(JPushInterface.EXTRA_ALERT);
            Gson gson = GsonBuilderUtil.create();
            Map<String,String> pushMap = gson.fromJson(extraJson, new TypeToken<Map<String, String>>() {
            }.getType());
            String code = pushMap.get("code");
            switch (code){
                case "1" :
                    mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

                    break;
            }

        }
    }
}
