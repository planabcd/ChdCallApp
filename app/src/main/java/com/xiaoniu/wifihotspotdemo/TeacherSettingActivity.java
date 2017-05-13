package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.common.ActivityCollector;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.util.AttenceWifiUtil;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by think on 2017/5/9 15:13
 */

public class TeacherSettingActivity extends BaseActivity implements View.OnClickListener {
    private TextView mTvBack;
    private RelativeLayout mRlSpecilCall;
    private RelativeLayout mRlAbout;
    private RelativeLayout mRlSuggesion;
    private Button mBtnExitLogin;

    private RelativeLayout mRlBindMac;
    private TextView mTvBindMac;



    private Teacher mTeacher;
    private AttenceWifiUtil mAttenceWifiUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_setting);
        mAttenceWifiUtil = AttenceWifiUtil.getInstance(this);
        initView();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mRlSpecilCall = (RelativeLayout) findViewById(R.id.rl_specil_call);
        mRlAbout = (RelativeLayout) findViewById(R.id.rl_about);
        mRlSuggesion = (RelativeLayout) findViewById(R.id.rl_suggesion);
        mBtnExitLogin = (Button) findViewById(R.id.btn_exit_login);

        mRlBindMac = (RelativeLayout) findViewById(R.id.rl_bind_mac);
        mTvBindMac = (TextView) findViewById(R.id.tv_bind_mac);

        mTvBack.setOnClickListener(this);
        mRlSpecilCall.setOnClickListener(this);
        mRlAbout.setOnClickListener(this);
        mRlSuggesion.setOnClickListener(this);
        mBtnExitLogin.setOnClickListener(this);
        mRlBindMac.setOnClickListener(this);


        String json = PrefUtils.getString(this, "teacher", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"登录失效,请重新登录");
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        mTeacher = gson.fromJson(json, Teacher.class);
        String macAddress = mTeacher.getMacAddress();
        if(!TextUtils.isEmpty(macAddress)){
            mTvBindMac.setText("MAC_ADDRESS:"+macAddress);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.rl_specil_call:
                specilCall();
                break;
            case R.id.rl_about:
                about();
                break;
            case R.id.rl_suggesion:
                suggesion();
                break;
            case R.id.btn_exit_login:
                exit();
                break;
            case R.id.rl_bind_mac:
                bindMac();
                break;
        }
    }

    /**
     * 绑定mac地址
     */
    private void bindMac() {
        String s = mTvBindMac.getText().toString();
        if(TextUtils.isEmpty(s) || s.contains(":") || "绑定mac地址".equals(s)){
            UIUtil.alert(this, "请确认","是否重新绑定mac地址", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    final String macAddress = mAttenceWifiUtil.getMacAddress();
                    if(TextUtils.isEmpty(macAddress)){
                        UIUtil.alert(TeacherSettingActivity.this, "无法获取mac地址","请连接有效网络或者打开热点尝试重新获取", new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                            }
                        });
                        return;
                    }
                    UIUtil.ok(TeacherSettingActivity.this, "获取mac地址成功","是否绑定", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                            Map<String,String> params = new HashMap<String,String>();
                            params.put("teacherId",mTeacher.getTeacherId()+"");
                            params.put("macAddress",macAddress);
                            NetWorkUtil.post(Constant.URL_TEACHER_BIND_MACADDRESS, params, new NetWorkUtil.Worker() {
                                @Override
                                public void success(final String result, Gson gson) {
                                    mTvBindMac.setText("MAC_ADDRESS:"+macAddress);

                                    PrefUtils.setString(getApplication(),"teacher",result);
                                    UIUtil.okNoCancel(TeacherSettingActivity.this, "请确认", "绑定教师端mac地址成功", new UIUtil.AlterCallBack() {
                                        @Override
                                        public void confirm() {
                                        }
                                    });

                                }
                            });
                        }
                    });

                }
            });
        }
    }

    /**
     * 考勤异常申请
     */
   private void specilCall() {
        Intent intent = new Intent(this, TeacherHistoryActivity.class);
        startActivity(intent);
    }

    private void exit() {
        UIUtil.alert(this, "是否退出", "请确认", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                ActivityCollector.finishAll();
                Intent intent = new Intent(TeacherSettingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void suggesion() {
        UIUtil.okTips(this, "意见反馈", "正在开发...");
    }

    /**
     * 关于
     */
    private void about() {
        UIUtil.okTips(this, "版本信息", "长安大学点名系统V1.0");
    }
}
