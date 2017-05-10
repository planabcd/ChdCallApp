package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by think on 2017/5/6 15:32.
 */

public class LocationInfo {
    private String latitude; //获取纬度信息
    private String lontitude; //获取经度信息
    private String time; //定位时间
    private String errorType; //错误类型
    private String addr; //大致地址
    private String curPOILocAddress;

    public String getCurPOILocAddress() {
        return curPOILocAddress;
    }

    public void setCurPOILocAddress(String curPOILocAddress) {
        this.curPOILocAddress = curPOILocAddress;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLontitude() {
        return lontitude;
    }

    public void setLontitude(String lontitude) {
        this.lontitude = lontitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}
