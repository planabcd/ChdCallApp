package com.xiaoniu.wifihotspotdemo.util;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Map;

/**
 * Created by think on 2017/5/6 10:42
 * 请求网络
 */

public class NetWorkUtil {
    public static void post(final String url, Map<String,String> reqData, final Worker worker) {
        RequestParams params = new RequestParams(url);
        if(reqData!=null){
            for (String key : reqData.keySet()) {
                params.addQueryStringParameter(key,reqData.get(key));
            }
        }
        final Gson gson = GsonBuilderUtil.create();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(TextUtils.isEmpty(result) || result.length()<3){
                    Toast.makeText(x.app(), "服务器暂无结果返回", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.i("TAG","url="+url+",服务器返回结果:"+result);
                worker.success(result,gson);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), "服务器繁忙,请稍后再试", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    public static void postYN(final String url, Map<String,String> reqData, final WorkerYN workerYN) {
        RequestParams params = new RequestParams(url);
        if(reqData!=null){
            for (String key : reqData.keySet()) {
                params.addQueryStringParameter(key,reqData.get(key));
            }
        }
        final Gson gson = GsonBuilderUtil.create();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","url="+url+",服务器返回结果:"+result);
                workerYN.success(result,gson);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), "服务器繁忙,请稍后再试", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    public interface Worker{
        void success(String result,Gson gson);
    }

    public interface WorkerYN{
        void success(String result,Gson gson);
    }
}
