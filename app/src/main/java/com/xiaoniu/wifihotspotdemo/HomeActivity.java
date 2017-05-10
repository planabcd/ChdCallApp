package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.mingle.widget.ShapeLoadingDialog;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout mLlStartCall;
    private LinearLayout mLlKq;
    private LinearLayout mLlPerson;
    private LinearLayout mLlOther;
    private LinearLayout mLlEr;
    private ShapeLoadingDialog shapeLoadingDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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



}
