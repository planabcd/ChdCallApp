package com.xiaoniu.wifihotspotdemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by think on 2017/5/11 9:30.
 */

public class WifiWifiManager {
    private Context mContext;
    private android.net.wifi.WifiManager wifiManager ;
    public static boolean isInOpenWifi=false;

    public WifiWifiManager(Context context){
        this.mContext=context;
        wifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 扫描wifi
     */
    public boolean startScan(){
        return wifiManager.startScan();
    }

    /**
     * 判断wifi是否打开
     * @return
     */
    public boolean isWifiActive(){
        ConnectivityManager mConnectivity=(ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivity!=null){
            NetworkInfo[] infos=mConnectivity.getAllNetworkInfo();
            if (infos !=null){
                for (NetworkInfo ni:infos){
                    if ("WIFI".equals(ni.getTypeName())&&ni.isConnected())
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 打开wifi
     */
    public void openWifi(){
        if (!isWifiActive())
            wifiManager.setWifiEnabled(true);
    }

    /**
     * 关闭wifi
     */
    public void closeWifi(){
        if (isWifiActive())
            wifiManager.setWifiEnabled(false);
    }

    /**
     * 记录进入软件时的wifi,
     * 若开启，离开软件时开启wifi
     */
    public void InApp_isOpenWifi(){
        if (isWifiActive())
            isInOpenWifi = true;
    }

    /**
     * 获得热点手机IP地址
     * @return ip
     */
    public String getIp(){
        DhcpInfo info=wifiManager.getDhcpInfo();
        int iii=info.serverAddress;
        String ip=intToIp(iii);
        return ip;
    }

    /**
     * 获得ip地址算法
     * @param i
     * @return IP
     */
    private String intToIp(int i){
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

}