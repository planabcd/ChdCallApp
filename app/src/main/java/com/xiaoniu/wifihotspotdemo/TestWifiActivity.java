package com.xiaoniu.wifihotspotdemo;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaoniu.wifihotspotdemo.util.MacUtil;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;
import com.xiaoniu.wifihotspotdemo.util.WifiAPManager;
import com.xiaoniu.wifihotspotdemo.util.WifiWifiManager;


public class TestWifiActivity extends AppCompatActivity{

    private WifiManager wifiManager;
    private WifiAPManager wifiAPManager;
    private WifiWifiManager wifiWifiManager;

    private EditText mEtWifiName;
    private EditText mEtConnectName;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_test_wifi);
        initTest();
    }




    /**
     * 测试wifi
     */
    private void initTest() {
        mEtWifiName = (EditText) findViewById(R.id.et_wifi_name);
        mEtConnectName = (EditText) findViewById(R.id.et_connect_name);

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiAPManager = new WifiAPManager(this);
        wifiWifiManager = new WifiWifiManager(this);
    }

    /**
     * 检测是否开启gps
     * @param v
     */
    public void getIsGps(View v){
        UIUtil.showToastS(this,"get isaccess gps click");
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
            //GPS正常开启，可以写入需要开启GPS才能执行的方法
            //也可在onActivityResult()方法写
        } else {
            Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
            //调转GPS设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //此为设置完成后返回到获取界面
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // ............自己需要执行的方法

            LocationManager locationManager = (LocationManager) this
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
                //GPS正常开启，可以写入需要开启GPS才能执行的方法
                //也可在onActivityResult()方法写
            } else {
                Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
                //调转GPS设置界面
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //此为设置完成后返回到获取界面
                startActivityForResult(intent, 1);
            }
        }
    }

    /**
     * 创建热点
     */
    public void craeteNoPwd(View v) {
        UIUtil.showToastS(this,"craeteNoPwd click");
        String s = mEtWifiName.getText().toString();
        if(!TextUtils.isEmpty(s)){
            if(wifiWifiManager.isWifiActive()){
                wifiWifiManager.closeWifi();
            }
            wifiAPManager.startWifiAp(s,"",true);
        }

    }
    /**
     * 获取mac地址
     */
    public void getMac(View v) {
        UIUtil.showToastS(this,"getMac click");
        if(wifiWifiManager.isWifiActive()){
            wifiWifiManager.closeWifi();
        }
        wifiAPManager.startWifiAp("test","",true);
        String s = MacUtil.recupAdresseMAC(wifiManager);
        if(!MacUtil.marshmallowMacAddress.equals(s)){
            UIUtil.showToastS(this,"mac:"+s);
        }else{
            UIUtil.showToastS(this,"mac none");
        }
    }

    /**
     * 获取mac地址,通过wifi
     */
    public void getMacWifi(View v) {
        UIUtil.showToastS(this,"getMacWifi click");
        if(!wifiWifiManager.isWifiActive()){
            wifiWifiManager.openWifi();
            String s = MacUtil.recupAdresseMAC(wifiManager);
            if(!MacUtil.marshmallowMacAddress.equals(s)){
                UIUtil.showToastS(this,"mac:"+s);
            }else{
                UIUtil.showToastS(this,"mac none");
            }
        }
        UIUtil.showToastS(this,"mac none");
    }

    /**
     * 关闭热点
     */
    public void closeNoPwd(View v) {
        UIUtil.showToastS(this,"closeNoPwd click");
        if(wifiAPManager.isWifiApEnabled()){
            wifiAPManager.closeWifiAp();
        }
    }

    /**
     * 获取bssid
     */
    public void getNoPwdSSID(View v) {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null) {
            String bssid = connectionInfo.getBSSID();
            UIUtil.showToastS(this,"bssid:"+bssid);
        }else{
            UIUtil.showToastS(this,"con is null");
        }
    }
    /**
     * 连接指定wifi
     */
    public void connectNoPwd(View v) {
        UIUtil.showToastS(this,"connectNoPwd click");
        String s = mEtConnectName.getText().toString();
        if(TextUtils.isEmpty(s)) {
           return;
        }
        if(wifiAPManager.isWifiApEnabled()){
           wifiAPManager.closeWifiAp();
        }
        if(!wifiWifiManager.isWifiActive()){
            wifiWifiManager.openWifi();
        }
        connectNoPwdSSID(s);



    }

    /**
     * 连接指定wifi
     */
    public void connectNoPwdSSID(String ssid){
        wifiManager.disconnect();
        WifiConfiguration config = createWifiInfo(ssid);
        connect(config);
    }

    public WifiConfiguration createWifiInfo(String SSID) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        config.wepKeys[0] = "\"" + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.wepTxKeyIndex = 0;
        return config;
    }

    private void connect(WifiConfiguration config) {
        int wcgID = wifiManager.addNetwork(config);
        wifiManager.enableNetwork(wcgID, true);
        wifiManager.saveConfiguration();
    }

}
