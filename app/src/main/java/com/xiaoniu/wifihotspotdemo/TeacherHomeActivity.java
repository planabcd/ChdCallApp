package com.xiaoniu.wifihotspotdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.LocationInfo;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.util.ErUtil;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TeacherHomeActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlStartCall;
    private LinearLayout mLlKq;
    private LinearLayout mLlPerson;
    private LinearLayout mLlEr;
    private LinearLayout mLlSetting;
    private Teacher mTeacher;
    private LinearLayout mLlLoc;
    private LocationManager mLocationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_teacher);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        initView();
    }

    private void initView() {
        String json = PrefUtils.getString(this, "teacher", null);
        if (TextUtils.isEmpty(json)) {
            UIUtil.showToast(this, "登录失效,请重新登录");
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
        mLlLoc = (LinearLayout) findViewById(R.id.ll_loc);
        mLlSetting = (LinearLayout) findViewById(R.id.ll_setting);

        mLlStartCall.setOnClickListener(this);
        mLlKq.setOnClickListener(this);
        mLlPerson.setOnClickListener(this);
        mLlEr.setOnClickListener(this);
        mLlLoc.setOnClickListener(this);
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
            case R.id.ll_loc:
                loc();
                break;
            case R.id.ll_setting:
                setting();
                break;
        }
    }

    /**
     * 定位并推送到学生端进行测距
     */
    private void loc() {
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //如果GPS未打开
            UIUtil.alert(this, "请开启GPS","否则无法进行准确定位", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    //调转GPS设置界面
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //此为设置完成后返回到获取界面
                    startActivityForResult(intent, 1);
                }
            });
        }else{
            Intent intent = new Intent(this, LocationActivity.class);
            startActivityForResult(intent,0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(data==null){
                UIUtil.showToastS(this,"暂未获取到位置信息");
                return;
            }
            String locationInfo = data.getStringExtra("locationInfo");
            Gson gson = GsonBuilderUtil.create();
            final LocationInfo location = gson.fromJson(locationInfo, LocationInfo.class);
            String curPOILocAddress = location.getCurPOILocAddress();
            if(TextUtils.isEmpty(curPOILocAddress)){
                UIUtil.showToastS(this,"暂未获取到位置信息");

            }else{
                UIUtil.ok(this, "是否推送测距","当前位置信息:"+curPOILocAddress, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        String latitude = location.getLatitude();
                        String lontitude = location.getLontitude();
                        Map<String, String> reqData = new HashMap<>();
                        reqData.put("latitude",latitude);
                        reqData.put("lontitude",lontitude);
                        NetWorkUtil.post(Constant.URL_REQ_PUSH_TEACHER_LOCINFO, reqData, new NetWorkUtil.Worker() {
                            @Override
                            public void success(String result, Gson gson) {
                                if(result.contains("success")){
                                    UIUtil.okTips(TeacherHomeActivity.this,"请确认","推送成功,请等待测距结果通知");
                                }
                            }
                        });
                    }
                });
            }
        }else if(requestCode==1){
            loc();
        }

    }

    /**
     * 生成考勤信息二维码
     */
    private void setEr() {
        String url = "http://www.baidu.com";
        Bitmap bitmap = ErUtil.encodeAsBitmap(url);
        Drawable drawable = new BitmapDrawable(bitmap);
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("扫描完成考勤")
                .setCustomImage(drawable)
                .show();
    }


    /**
     * 个人信息
     */
    private void personInfo() {
        UIUtil.okTips(this, "个人信息", "工号:" + mTeacher.getTeacherId() + "\n姓名:" + mTeacher.getName());
    }

    /**
     * 教师进入考勤配置界面
     */
    private void startCall() {
        String json = PrefUtils.getString(this, "teacher", null);
        if (TextUtils.isEmpty(json)) {
            UIUtil.showToast(this, "登录失效,请重新登录");
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        mTeacher = gson.fromJson(json, Teacher.class);

        String macAddress = mTeacher.getMacAddress();
        if (TextUtils.isEmpty(macAddress)) {
            UIUtil.alert(TeacherHomeActivity.this, "请先绑定mac地址", "如果不绑定mac地址无法进行考勤", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    Intent it = new Intent(TeacherHomeActivity.this, TeacherSettingActivity.class);
                    startActivity(it);
                }
            });
        } else {
            Intent it = new Intent(this, TeacherPreAttenceActivity.class);
            startActivity(it);
        }
    }

    /**
     * 其他设置
     */
    private void setting() {
        Intent it = new Intent(this, TeacherSettingActivity.class);
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
