package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.domain.LocationInfo;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import java.util.List;

public class LocationActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvBack;
    private TextView mTvRefresh;
    private MapView mBmapView;
    public LocationClient mLocationClient;
    public BDLocationListener myListener;
    private LatLng curLatLng;
    private BaiduMap mBaiduMap;
    private volatile Boolean isFirstLoc = true;
    private Marker marker;
    private LocationInfo mLocationInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化地图
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);
        initView();
        initMap();
    }

    /**
     * 初始化地图定位
     */
    private void initMap() {
        myListener = new MyLocationListener();
        mBaiduMap = mBmapView.getMap();
        //设置缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //初始化地图配置
        initLocation();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                if(m!=null && m.equals(marker)){
                    //展示当前所在位置
                    UIUtil.showToast(LocationActivity.this,mLocationInfo.getCurPOILocAddress());
                }
                return false;
            }
        });
        //开始定位
        mLocationClient.start();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mBmapView = (MapView) findViewById(R.id.bmapView);

        mTvBack.setOnClickListener(this);
        mTvRefresh.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.tv_refresh:
                refreshLocation();
                break;
        }
    }

    private void back(){
        if(mLocationInfo==null){
            UIUtil.alert(LocationActivity.this,"确认退出?","尚未获取到位置信息,请稍等", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    LocationActivity.this.setResult(10);
                    finish();
                }
            });
        }else{
            Intent it = new Intent();
            it.putExtra("locationInfo",GsonBuilderUtil.create().toJson(mLocationInfo));
            LocationActivity.this.setResult(10,it);
            finish();
        }
    }

    /**
     * 刷新当前位置
     */
    private void refreshLocation() {
        if(mLocationClient!=null){
            UIUtil.showToastS(this,"正在重新获取我的位置,请稍等");
            mLocationClient.stop();
            isFirstLoc = true;
            mLocationClient.start();
        }
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


    /**
     * Created by think on 2017/5/5 19:24
     * 百度地图定位监听
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocationInfo = new LocationInfo();
            mLocationInfo.setTime(location.getTime());
            mLocationInfo.setLatitude(String.valueOf(location.getLatitude()));
            mLocationInfo.setLontitude(String.valueOf(location.getLongitude()));
            switch (location.getLocType()){
                case BDLocation.TypeGpsLocation :
                    mLocationInfo.setErrorType("gps定位成功");
                    mLocationInfo.setAddr(location.getAddrStr());
                    break;
                case BDLocation.TypeNetWorkLocation:
                    mLocationInfo.setErrorType("网络定位成功");
                    mLocationInfo.setAddr(location.getAddrStr());
                    break;
                case BDLocation.TypeOffLineLocation:
                    mLocationInfo.setErrorType("离线定位成功");
                    break;
                case BDLocation.TypeServerError:
                    mLocationInfo.setErrorType("服务端网络定位失败");
                    break;
                case BDLocation.TypeNetWorkException:
                    mLocationInfo.setErrorType("请检查网络是否通畅");
                    break;
                case BDLocation.TypeCriteriaException:
                    mLocationInfo.setErrorType("无法获取有效定位依据导致定位失败");
                    break;
                default:
                    break;
            }
            List<Poi> list = location.getPoiList();    // POI数据
            if (list != null && list.size()!=0) {
                mLocationInfo.setCurPOILocAddress(list.get(list.size()-1).getName()+"附近");
            }
            //将当前定位的位置设置为地图中心
            if (location == null || mBmapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            //定义Maker坐标点
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_loc3);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.clear();
            marker = (Marker) mBaiduMap.addOverlay(option);

            if (isFirstLoc) {
                isFirstLoc = false;
                curLatLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(curLatLng);
                mBaiduMap.animateMapStatus(u);
            }
        }
        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }


    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span=2000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mBmapView.onDestroy()，实现地图生命周期管理
        mBmapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mBmapView. onResume ()，实现地图生命周期管理
        mBmapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mBmapView. onPause ()，实现地图生命周期管理
        mBmapView.onPause();
    }
}
