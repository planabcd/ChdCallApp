package com.xiaoniu.wifihotspotdemo.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * Created by think on 2017/5/11 10:49
 * 点名系统wifi工具类,单例
 */

public class AttenceWifiUtil {
    private static volatile AttenceWifiUtil attenceWifiUtil = null;

    private Context mContext;
    private WifiManager wifiManager;
    private WifiAPManager wifiAPManager;
    private WifiWifiManager wifiWifiManager;

    public static AttenceWifiUtil getInstance(Context ctx){
        if(attenceWifiUtil==null){
            synchronized (AttenceWifiUtil.class){
                if(attenceWifiUtil==null){
                    attenceWifiUtil = new AttenceWifiUtil(ctx);
                }
            }

        }
        return attenceWifiUtil;
    }
    private AttenceWifiUtil(Context ctx){
        this.mContext = ctx;
        init();
    }


    /**
     * 初始化引用的工具类
     */
    private void init() {
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiAPManager = new WifiAPManager(mContext);
        wifiWifiManager = new WifiWifiManager(mContext);
    }


    /**
     * 获取mac地址
     * @return
     */
    public String getMacAddress(){
        if(wifiWifiManager.isWifiActive()){
            wifiWifiManager.closeWifi();
        }
        wifiAPManager.startWifiAp("GetMacTestName","",true);
        String s = MacUtil.recupAdresseMAC(wifiManager);
        if(wifiAPManager.isWifiApEnabled()){
            wifiAPManager.closeWifiAp();
        }
        if(!MacUtil.marshmallowMacAddress.equals(s)){
            return s;
        }
        return "";
    }

    /**
     * 创建热点
     */
    public boolean craeteNoPwdAP(String ssid) {
        if(TextUtils.isEmpty(ssid)){
            return false;
        }
        //创建热点之前先关闭wifi
        if(wifiWifiManager.isWifiActive()){
            wifiWifiManager.closeWifi();
        }
        return wifiAPManager.startWifiAp(ssid,"",true);
    }

    /**
     * 关闭热点
     */
    public void closeNoPwdAP() {
        if(wifiAPManager.isWifiApEnabled()){
            wifiAPManager.closeWifiAp();
        }
    }

    public boolean isApActivie(){
        return wifiAPManager.isWifiApEnabled();
    }

    /**
     * 获取bssid
     */
    public String getBSSID() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if(connectionInfo==null){
            return "";
        }
        return connectionInfo.getBSSID();
    }
    /**
     * 连接指定wifi
     */
    public void connectNoPwdAP(String ssid) {
        if(TextUtils.isEmpty(ssid)) {
            return;
        }
        if(wifiAPManager.isWifiApEnabled()){
            wifiAPManager.closeWifiAp();
        }
        if(!wifiWifiManager.isWifiActive()){
            wifiWifiManager.openWifi();
        }
        connectNoPwdSSID(ssid);
    }

    /**
     * 连接指定wifi
     */
    private void connectNoPwdSSID(String ssid){
        wifiManager.disconnect();
        WifiConfiguration config = createWifiInfo(ssid);
        connect(config);
    }

    private WifiConfiguration createWifiInfo(String SSID) {
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

