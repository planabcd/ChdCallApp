package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mingle.widget.ShapeLoadingDialog;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlStartCall;
    private LinearLayout mLlKq;
    private LinearLayout mLlPerson;
    private LinearLayout mLlOther;
    private LinearLayout mLlEr;
    private ShapeLoadingDialog shapeLoadingDialog;
    private Set<String> jpushTags;
    private String jpushAlias;
    private Student mStudent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        jpushTags = new HashSet<>();
        JPushInterface.init(this);
        initView();
    }

    private void initView() {
        mLlStartCall = (LinearLayout) findViewById(R.id.ll_start_call);
        mLlKq = (LinearLayout) findViewById(R.id.ll_kq);
        mLlPerson = (LinearLayout) findViewById(R.id.ll_person);
        mLlOther = (LinearLayout) findViewById(R.id.ll_other);
        mLlEr = (LinearLayout) findViewById(R.id.ll_er);


        mLlStartCall.setOnClickListener(this);
        mLlKq.setOnClickListener(this);
        mLlPerson.setOnClickListener(this);
        mLlEr.setOnClickListener(this);
        mLlOther.setOnClickListener(this);



        String json = PrefUtils.getString(this, "student",null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"登录失效,请重新登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            Gson gson = GsonBuilderUtil.create();
            mStudent = gson.fromJson(json, Student.class);
        }
//        jpushAlias = PrefUtils.getString(this, "jpush_alias", null);
        jpushAlias = "";
        if(TextUtils.isEmpty(jpushAlias)){
            jpushTags.add(mStudent.getClassId()+"");
            setAlias(mStudent.getStuId()+"");
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_start_call:
                startCall();
                break;
            case R.id.ll_kq:
                attenceHistory();
                break;
            case R.id.ll_person:
                personInfo();
                break;
            case R.id.ll_er:
                er();
                break;
            case R.id.ll_other:
                otherSetting();
                break;
        }
    }

    /**
     * 扫一扫考勤
     */
    private void er() {
        UIUtil.alert(this, "请确认","是否测试推送", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                Map<String, String> reqData = new HashMap<>();
                NetWorkUtil.post(Constant.URL_REQ_PUSH_TEST, reqData, new NetWorkUtil.Worker() {
                    @Override
                    public void success(String result, Gson gson) {

                    }
                });
            }
        });
    }


    /**
     * 学生进入考勤配置界面
     */
    private void startCall() {
        Intent intent = new Intent(this, StudentPreAttenceActivity.class);
        startActivity(intent);
    }
    /**
     * 其他设置
     */
    private void otherSetting() {
        //        CatLoadingView mView;
        //        mView = new CatLoadingView();
        //        mView.show(getSupportFragmentManager(), "");
        //        Intent intent = new Intent(this, AttenceActivity.class);
        //                startActivity(intent);
        Intent intent = new Intent(this, StudentSettingActivity.class);
        startActivity(intent);
       /* shapeLoadingDialog=new ShapeLoadingDialog(this);
        shapeLoadingDialog.setCanceledOnTouchOutside(false);
        shapeLoadingDialog.setLoadingText("加载中...");
        shapeLoadingDialog.show();*/
    }


    /**
     * 个人信息
     */
    private void personInfo() {
        Intent intent = new Intent(this, StudentInfoActivity.class);
        startActivity(intent);
    }

    /**
     * 学生考勤历史记录
     */
    private void attenceHistory() {
        Intent intent = new Intent(this, StudentHistoryActivity.class);
        startActivity(intent);
    }

    /**
     * 设置JPush别名
     * @param alias
     */

    private void setAlias(String alias) {
        if (TextUtils.isEmpty(alias)) {
            Toast.makeText(HomeActivity.this,R.string.error_alias_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        // 调用 Handler 来异步设置别名
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i("JPUSHTAG", logs);
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    PrefUtils.setString(HomeActivity.this,"jpush_alias",alias);
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i("JPUSHTAG", logs);
                    // 延迟 60 秒来调用 Handler 设置别名
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e("JPUSHTAG", logs);
            }
            UIUtil.showToast(HomeActivity.this,logs);
        }
    };
    private static final int MSG_SET_ALIAS = 1001;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d("JPUSHTAG", "Set alias in handler.");
                    // 调用 JPush 接口来设置别名。
                    JPushInterface.setAliasAndTags(getApplicationContext(),
                            (String) msg.obj,
                            jpushTags,
                            mAliasCallback);
                    break;
                default:
                    Log.i("JPUSHTAG", "Unhandled msg - " + msg.what);
            }
        }
    };



}
