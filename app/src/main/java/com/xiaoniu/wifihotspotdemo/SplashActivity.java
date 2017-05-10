package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout mLlStartCall;
    private LinearLayout mLlKq;
    private LinearLayout mLlPerson;
    private LinearLayout mLlClass;
    private LinearLayout mLlOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
    }

    private void initView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);
    }


    @Override
    public void onClick(View v) {
       /* switch (v.getId()) {
            case R.id.ll_start_call:
                startCall();
                break;
        }*/
    }



}
