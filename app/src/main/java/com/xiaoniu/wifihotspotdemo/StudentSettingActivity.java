package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaoniu.wifihotspotdemo.util.UIUtil;




/**
 * Created by think on 2017/5/9 15:13.
 */

public class StudentSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvBack;
    private RelativeLayout mRlSpecilCall;
    private RelativeLayout mRlAbout;
    private RelativeLayout mRlSuggesion;
    private Button mBtnExitLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_setting);
        initView();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mRlSpecilCall = (RelativeLayout) findViewById(R.id.rl_specil_call);
        mRlAbout = (RelativeLayout) findViewById(R.id.rl_about);
        mRlSuggesion = (RelativeLayout) findViewById(R.id.rl_suggesion);
        mBtnExitLogin = (Button) findViewById(R.id.btn_exit_login);

        mTvBack.setOnClickListener(this);
        mRlSpecilCall.setOnClickListener(this);
        mRlAbout.setOnClickListener(this);
        mRlSuggesion.setOnClickListener(this);
        mBtnExitLogin.setOnClickListener(this);

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
        }
    }

    /**
     * 考勤异常申请
     */
    private void specilCall() {
        Intent intent = new Intent(this, StudentHistoryActivity.class);
        startActivity(intent);
    }

    private void exit() {
        UIUtil.alert(this, "是否退出", "请确认", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                Intent intent = new Intent(StudentSettingActivity.this, LoginActivity.class);
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
