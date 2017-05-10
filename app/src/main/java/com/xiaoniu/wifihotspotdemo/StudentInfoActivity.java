package com.xiaoniu.wifihotspotdemo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.Map;



public class StudentInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvBack;
    private TextView mTvName;
    private TextView mTvStudentId;
    private TextView mTvMarjor;
    private TextView mTvAcademy;
    private LinearLayout mLlMac;
    private TextView mTvMac;
    private Student mStudent;
    private WifiManager mWifiManager;
    private String mMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);
        initData();
        initView();
    }


    private void initData() {
        String json = PrefUtils.getString(this, "student", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"当前登录信息失效,请重新登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        mStudent = gson.fromJson(json, Student.class);
    }

    private void initView() {
        mTvBack = (TextView) findViewById( R.id.tv_back);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvStudentId = (TextView) findViewById(R.id.tv_student_id);
        mTvMarjor = (TextView) findViewById(R.id.tv_marjor);
        mTvAcademy = (TextView) findViewById(R.id.tv_academy);
        mLlMac = (LinearLayout) findViewById(R.id.ll_mac);
        mTvMac = (TextView) findViewById(R.id.tv_mac);

        mLlMac.setOnClickListener(this);
        mTvBack.setOnClickListener(this);

        mTvName.setText(mStudent.getName());
        mTvMarjor.setText(mStudent.getMajor());
        mTvAcademy.setText(mStudent.getAcademy());
        mTvStudentId.setText(mStudent.getStuId()+"");
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mMacAddress = PrefUtils.getString(this, "macAddress", null);
        if(TextUtils.isEmpty(mMacAddress)){
            //未绑定mac地址
            getMacAddress();
        }else{
            mTvMac.setText(mMacAddress);
        }
    }

    /**
     * 获取mac地址,如果获取成功,绑定操作
     */
    private void getMacAddress() {
        String mac = mWifiManager.getConnectionInfo().getMacAddress();
        if(TextUtils.isEmpty(mac)){
            UIUtil.alert(this, "无法获取mac地址","请连接有效wifi网络,再绑定mac地址" , new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                }
            });
            return;
        }
        bindMacAddress(mac);
    }

    private void bindMacAddress(final String macAddress){
        Map<String,String> params = new HashMap<String,String>();
        params.put("studentId",mStudent.getStuId()+"");
        params.put("macAddress",macAddress);
        NetWorkUtil.post(Constant.URL_STUDNET_BIND_MAC_ADDRESS, params, new NetWorkUtil.Worker() {
            @Override
            public void success(String result, Gson gson) {
                Student student = gson.fromJson(result, Student.class);
                UIUtil.okNoCancel(StudentInfoActivity.this, "绑定mac地址成功", "macAddress:"+macAddress, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        //保存mac地址
                        PrefUtils.setString(StudentInfoActivity.this,"macAddress",macAddress);
                        mTvMac.setText(macAddress);
                    }
                });

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.ll_mac:
                String mac = mTvMac.getText().toString();
                if(TextUtils.isEmpty(mac) || !mac.contains(":")){
                    getMacAddress();
                }else{
                    UIUtil.showToastS(this,"已经绑定mac地址,无需重复绑定");
                }
                break;
        }
    }



}
