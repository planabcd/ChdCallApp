package com.xiaoniu.wifihotspotdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.adapter.WifiListAdapter;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttenceVO;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttenceVO;
import com.xiaoniu.wifihotspotdemo.util.AttenceWifiUtil;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentMainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvBack;
    private TextView mTvRefresh;
    private TextView mTextState;
    private ListView mLvWifiinfo;

    private WifiListAdapter wifiListAdapter;
    private boolean mFirstCall = true;
    private TeacherAttenceVO teacherAttence;

    private String wifiName;
    private Student mStudent;
    private String macAddress;
    private AttenceWifiUtil mAttenceWifiUtil;
    private android.net.wifi.WifiManager wifiManager ;
    //如果连接教师热点成功,isSuccess = true
    private volatile boolean isSuccess = false;
    private volatile boolean isUpload = false;

    private TextView mTextWifiName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        mAttenceWifiUtil= AttenceWifiUtil.getInstance(this);
        wifiManager=(WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        initData();
        initView();
        initBroadcastReceiver();
        //开始扫描wifi
        mAttenceWifiUtil.search();
    }

    private void initData() {
        Gson gson = GsonBuilderUtil.create();
        String json = PrefUtils.getString(this, "student",null);
        if(TextUtils.isEmpty(json)) {
            UIUtil.showToast(this, "登录失效,请重新登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        mStudent = gson.fromJson(json, Student.class);

        Intent intent = getIntent();
        String json2 = intent.getStringExtra("student_teacherAttence");
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"暂未获取考勤信息,请稍后重试...");
            return;
        }
        teacherAttence = gson.fromJson(json2, TeacherAttenceVO.class);
        macAddress = teacherAttence.getMacAddress();
        wifiName = teacherAttence.getWifiName();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mTextState = (TextView) findViewById(R.id.text_state);
        mLvWifiinfo = (ListView) findViewById(R.id.lv_wifiinfo);
        mTextWifiName = (TextView) findViewById(R.id.text_wifi_name);
        mTextWifiName.setText("教师热点:"+wifiName);

        mTvRefresh.setOnClickListener(this);
        mTvBack.setOnClickListener(this);

        wifiListAdapter = new WifiListAdapter(this, R.layout.wifi_list_item);
        mLvWifiinfo.setAdapter(wifiListAdapter);
        mLvWifiinfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ScanResult scanResult = wifiListAdapter.getItem(position);
            String ssid = scanResult.SSID;
            if(!TextUtils.isEmpty(ssid) && ssid.equals(wifiName)){
                if(!isSuccess){
                    UIUtil.alert(StudentMainActivity.this, "是否连接进行考勤","该热点为教师热点", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                            mAttenceWifiUtil.connectNoPwdAP(wifiName);
                        }
                    });
                    return;
                }
                if(!isUpload){
                    UIUtil.okTips(StudentMainActivity.this,"请上传考勤信息","如果未上传考勤信息,考勤结束后视为缺勤");
                    uploadCall();
                    return;
                }
                UIUtil.okTips(StudentMainActivity.this,"考勤成功","请勿重复尝试");
            }
            }
        });
    }


    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
    }


    /**
     * 重写返回键方法
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back(){
        if(isUpload){
            finish();
            return;
        }
        if(!isSuccess){
            UIUtil.alert(this, "是否退出","还未连接教师热点进行考勤,如果退出,考勤结束后视为缺勤", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    finish();
                }
            });
            return;
        }
        if(!isUpload){
            UIUtil.alert(this, "是否退出","还未上传考勤信息,如果退出,考勤结束后视为缺勤", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    finish();
                }
            });
            return;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_refresh:
                search();
                break;
            case R.id.tv_back:
                back();
                break;
        }
    }


    /**
     * 搜索wifi热点
     */
    private void search() {
        final boolean search = mAttenceWifiUtil.search();
        if(search){
            UIUtil.okTips(this,"开启wifi成功","正在扫描可用热点");
            return;
        }
        UIUtil.alert(this, "开启wifi失败","请重试或者手动开启wifi", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                search();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // wifi已成功扫描到可用wifi。
                List<ScanResult> scanResults = wifiManager.getScanResults();
                wifiListAdapter.clear();
                wifiListAdapter.addAll(scanResults);
                if(scanResults!=null && scanResults.size()!=0){
                    for(ScanResult s : scanResults){
                        if(wifiName.equals(s.SSID) && mFirstCall){
                            UIUtil.showToast(StudentMainActivity.this,"找到教师热点,正在连接");
                            mAttenceWifiUtil.connectNoPwdAP(wifiName);
                            return;
                        }
                    }
                }
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        UIUtil.showToastS(StudentMainActivity.this,"wifi已打开");
                        //获取到wifi开启的广播时，开始扫描
//                        wifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
                        UIUtil.showToastS(StudentMainActivity.this,"wifi已关闭");
                        break;
                }
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    mTextState.setText("连接已断开");
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    mTextState.setText("已连接到网络:" + wifiInfo.getSSID());
                    if(wifiInfo.getSSID().trim().replaceAll("\"","").equals(wifiName)){
                        synchronized (StudentMainActivity.this){
                            if(isSuccess || isUpload){
                                return;
                            }
                            if(mFirstCall){
                                mFirstCall = false;
                            }
                            String bssid = wifiInfo.getBSSID();
//                            UIUtil.showToastS(StudentMainActivity.this,"教师端mac地址:"+bssid);
                            if(!TextUtils.isEmpty(bssid) && bssid.equals(macAddress)){
                                isSuccess = true;
                                UIUtil.okNoCancel(StudentMainActivity.this, "连接教师热点成功","请上传考勤信息,如果不上传,考勤结束后视为缺勤", new UIUtil.AlterCallBack() {
                                    @Override
                                    public void confirm() {
                                        if(!isUpload){
                                            uploadCall();
                                        }else{
                                            UIUtil.okTips(StudentMainActivity.this,"已经上传过考勤信息","请勿重试");
                                        }

                                    }
                                });
                            }
                        }
                    }
                } else {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                        mTextState.setText("连接中...");
                    } else if (state == state.AUTHENTICATING) {
                        mTextState.setText("正在验证身份信息...");
                    } else if (state == state.OBTAINING_IPADDR) {
                        mTextState.setText("正在获取IP地址...");
                    } else if (state == state.FAILED) {
                        mTextState.setText("连接失败");
                    }
                }

            }
        }
    };

    /**
     * 上传考勤信息
     */
    private void uploadCall() {
        final Map<String,String> reqData = new HashMap<String,String>();
        reqData.put("teacherAttenceId",String.valueOf(teacherAttence.getId()));
        reqData.put("studentId",String.valueOf(mStudent.getStuId()));
        NetWorkUtil.postYN(Constant.URL_LOGIN_STUDENT_DOCALL_ATTENCE, reqData, new NetWorkUtil.WorkerYN() {
            @Override
            public void success(String result, Gson gson) {
                if(TextUtils.isEmpty(result) || result.length()<3){
                    UIUtil.alert(StudentMainActivity.this, "考勤信息上传失败","未获取到考勤信息", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                    return;
                }
                StudentAttenceVO studentAttenceVO = gson.fromJson(result, StudentAttenceVO.class);
                Long state = studentAttenceVO.getState();
                if(state==3){
                    UIUtil.alert(StudentMainActivity.this, "考勤失败","当前考勤已经结束", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                    return;
                }

                String name = mStudent.getName();
                Long id = mStudent.getId();
                String courseName = studentAttenceVO.getCourseName();
                String remark = studentAttenceVO.getRemark();
                if(TextUtils.isEmpty(remark)){
                    remark = "无";
                }
                String msg = "课程:"+courseName+"\n学号:"+id+"\n"+"姓名:"+name+"\n备注:"+remark;
                UIUtil.ok(StudentMainActivity.this, "考勤成功,确认退出", msg, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        isUpload = true;
                        Intent intent = new Intent(StudentMainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });
    }




}
