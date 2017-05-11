package com.xiaoniu.wifihotspotdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.adapter.WifiListAdapter;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttence;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.x;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentMainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvBack;
    private TextView mTvRefresh;
    private TextView mTextState;
    private ListView mLvWifiinfo;

    private WifiManager wifiManager;
    private WifiListAdapter wifiListAdapter;
    private WifiConfiguration config;
    private int wcgID;
    private boolean mFirstCall = true;
    private TeacherAttence teacherAttence;

    private static final int WIFICIPHER_NOPASS = 1;
    private static final int WIFICIPHER_WEP = 2;
    private static final int WIFICIPHER_WPA = 3;
    private String wifiName;
    private String wifiPwd;
    private Student mStudent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        initData();
        initView();
        initBroadcastReceiver();
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
        teacherAttence = gson.fromJson(json2, TeacherAttence.class);
        wifiName = teacherAttence.getWifiName();
        wifiPwd = teacherAttence.getWifiPwd();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mTextState = (TextView) findViewById(R.id.text_state);
        mLvWifiinfo = (ListView) findViewById(R.id.lv_wifiinfo);

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
                    UIUtil.showToast(StudentMainActivity.this,"该热点为教师热点");
                }
            }
        });
    }

    private void connect(WifiConfiguration config) {
        mTextState.setText("连接中...");
        wcgID = wifiManager.addNetwork(config);
        wifiManager.enableNetwork(wcgID, true);
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_refresh:
                search();
                break;
            case R.id.tv_back:
                UIUtil.alert(this, "确认退出?", "还未上传考勤信息", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        finish();
                    }
                });
                break;
        }
    }


    /**
     * 搜索wifi热点
     */
    private void search() {
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
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
                            //找到教师开启的热点
                            mFirstCall = false;
                            UIUtil.showToast(StudentMainActivity.this,"找到教师热点,正在连接");
                            connectTeacher(s);
                        }
                    }
                }
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //获取到wifi开启的广播时，开始扫描
                        wifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
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
                        int networkId = wifiInfo.getNetworkId();
                        /*boolean hiddenSSID = wifiInfo.getHiddenSSID();
                        UIUtil.showToastS(StudentMainActivity.this,"hiddenSSID:"+hiddenSSID);
                        if(networkId!=1){
                            UIUtil.alert(StudentMainActivity.this, "请注意","你连接了假冒伪劣热点", new UIUtil.AlterCallBack() {
                                @Override
                                public void confirm() {
                                    //TODO
                                }
                            });
                            return;
                        }*/
                        //关闭wifi
                        /*if (wifiManager.isWifiEnabled()) {
                            //如果wifi处于打开状态，则关闭wifi,
                            wifiManager.setWifiEnabled(false);
                        }*/
                        //上传考勤信息
                        UIUtil.okNoCancel(StudentMainActivity.this, "考勤成功", "请确认是否上传考勤信息\n如果未上传会被视为缺勤", new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                                uploadCall();
                            }
                        });

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
        Map<String,String> reqData = new HashMap<String,String>();
        reqData.put("teacherAttenceId",String.valueOf(teacherAttence.getId()));
        reqData.put("studentId",String.valueOf(mStudent.getStuId()));
        NetWorkUtil.post(Constant.URL_LOGIN_STUDENT_DOCALL_ATTENCE, reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result, Gson gson) {
                StudentAttence studentAttence = gson.fromJson(result, StudentAttence.class);
                if(studentAttence==null) {
                    Toast.makeText(x.app(), "上传考勤信息错误,请联网后重试", Toast.LENGTH_LONG).show();
                    return;
                }
                String name = mStudent.getName();
                Long id = mStudent.getId();
                String msg = "学号:"+id+"\n"+"姓名:"+name;
                UIUtil.okNoCancel(StudentMainActivity.this, "考勤成功,确认退出", msg, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        Intent intent = new Intent(StudentMainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });
    }


    /**
     * 连接教师wifi
     * @param s
     */
    public void connectTeacher(ScanResult s){
        wifiManager.disconnect();
        String capabilities = s.capabilities;
        int type = WIFICIPHER_WPA;
        if (!TextUtils.isEmpty(capabilities)) {
            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                type = WIFICIPHER_WPA;
            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                type = WIFICIPHER_WEP;
            } else {
                type = WIFICIPHER_NOPASS;
            }
        }
        config = createWifiInfo(wifiName, wifiPwd, type);
        connect(config);
    }


    public WifiConfiguration createWifiInfo(String SSID, String password,
                                            int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }



}
