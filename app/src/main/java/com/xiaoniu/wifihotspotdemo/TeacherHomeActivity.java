package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.Map;

public class TeacherHomeActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlStartCall;
    private LinearLayout mLlKq;
    private LinearLayout mLlPerson;
    private LinearLayout mLlEr;
    private LinearLayout mLlSetting;
    private Teacher mTeacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_teacher);
        initView();
    }

    private void initView() {
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

        mLlStartCall = (LinearLayout) findViewById(R.id.ll_start_call);
        mLlKq = (LinearLayout) findViewById(R.id.ll_kq);
        mLlPerson = (LinearLayout) findViewById(R.id.ll_person);
        mLlEr = (LinearLayout) findViewById(R.id.ll_er);
        mLlSetting = (LinearLayout) findViewById(R.id.ll_setting);

        mLlStartCall.setOnClickListener(this);
        mLlKq.setOnClickListener(this);
        mLlPerson.setOnClickListener(this);
        mLlEr.setOnClickListener(this);
        mLlSetting.setOnClickListener(this);

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
                setEr();
                break;
            case R.id.ll_setting:
                setting();
                break;
        }
    }

    /**
     * 生成考勤信息二维码
     */
    private void setEr() {
    }




    /**
     * 个人信息
     */
    private void personInfo() {
        UIUtil.okTips(this,"个人信息","工号:"+mTeacher.getTeacherId()+"\n姓名:"+mTeacher.getName());
    }

    /**
     * 教师进入考勤配置界面
     */
    private void startCall() {
        final String macAddress = mTeacher.getMacAddress();
        if(TextUtils.isEmpty(macAddress)){
            Map<String,String> params = new HashMap<String,String>();
            params.put("teacherId",mTeacher.getTeacherId()+"");
            NetWorkUtil.post(Constant.URL_TEACHER_GET_MACADDRESS, params, new NetWorkUtil.Worker() {
                @Override
                public void success(String result, Gson gson) {
                    Teacher teacher = gson.fromJson(result, Teacher.class);
                    String netMacAddress = teacher.getMacAddress();
                    if(!TextUtils.isEmpty(netMacAddress)){
                        Intent it = new Intent(TeacherHomeActivity.this,TeacherSettingActivity.class);
                        startActivity(it);
                    }else{
                        UIUtil.alert(TeacherHomeActivity.this, "请先绑定mac地址","如果不绑定mac地址无法进行考勤", new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                                Intent it = new Intent(TeacherHomeActivity.this,TeacherSettingActivity.class);
                                startActivity(it);
                            }
                        });
                    }
                }
            });
            return;
        }
        Intent it = new Intent(this,TeacherPreAttenceActivity.class);
        startActivity(it);
    }
    /**
     * 其他设置
     */
    private void setting() {
        Intent it = new Intent(this,TeacherSettingActivity.class);
        startActivity(it);
    }

    /**
     * 教师考勤历史记录
     */
    private void attenceHistory() {
        Intent intent = new Intent(this, TeacherHistoryActivity.class);
        startActivity(intent);
    }



}
