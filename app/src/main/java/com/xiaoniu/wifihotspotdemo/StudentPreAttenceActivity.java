package com.xiaoniu.wifihotspotdemo;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mingle.widget.ShapeLoadingDialog;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Course;
import com.xiaoniu.wifihotspotdemo.domain.LocationInfo;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttenceVO;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.x;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StudentPreAttenceActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvBack;
    private TextView mTvConfirm;
    private TextView mTvStudentIdNameValue;
    private TextView mTvClassId;
    private RelativeLayout mRlSelectCourseId;
    private TextView mTvCourseName;
    private Student mStudent;

    private RelativeLayout mRlMyLocaltion;
    private TextView mTvMyLocaltionValue;
    private LocationManager mLocationManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_pre_attence);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        initData();
        initView();
    }



    public void initData(){
        String json = PrefUtils.getString(this, "student",null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"登录失效,请重新登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            Gson gson = GsonBuilderUtil.create();
            mStudent = gson.fromJson(json, Student.class);
        }
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvConfirm = (TextView) findViewById(R.id.tv_confirm);
        mTvStudentIdNameValue = (TextView) findViewById(R.id.tv_student_id_name_value);
        mTvClassId = (TextView) findViewById(R.id.tv_class_id);
        mRlSelectCourseId = (RelativeLayout) findViewById(R.id.rl_select_course_id);
        mTvCourseName = (TextView) findViewById(R.id.tv_course_name);
        mRlMyLocaltion = (RelativeLayout) findViewById(R.id.rl_my_localtion);
        mTvMyLocaltionValue = (TextView) findViewById(R.id.tv_my_localtion_value);

        mTvBack.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
        mRlSelectCourseId.setOnClickListener(this);
        mRlMyLocaltion.setOnClickListener(this);

        mTvStudentIdNameValue.setText(mStudent.getStuId()+"-"+mStudent.getName());
        mTvStudentIdNameValue.setTextColor(0xff202020);
        mTvClassId.setText(String.valueOf(mStudent.getClassId()));
        mTvClassId.setTextColor(0xff202020);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_select_course_id:
                selectCourse();
                break;
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_confirm:
                confirm();
                break;
            case R.id.rl_my_localtion:
                mylocation();
                break;
            default:
                break;
        }
    }

    /**
     * 获取我的位置
     */
    private void mylocation() {
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
                mTvMyLocaltionValue.setText("暂未获取到位置信息");
                return;
            }
            String locationInfo = data.getStringExtra("locationInfo");
            Gson gson = GsonBuilderUtil.create();
            LocationInfo location = gson.fromJson(locationInfo, LocationInfo.class);
            String curPOILocAddress = location.getCurPOILocAddress();
            mTvMyLocaltionValue.setText(curPOILocAddress);
            mTvMyLocaltionValue.setTextColor(0xff202020);
        }
        if(requestCode==1){
            mylocation();
        }

    }




    /**
     * 开启wifi准备连接热点
     */
    private void saveAttence(final TeacherAttenceVO teacherAttenceVO) {
        if(TextUtils.isEmpty(teacherAttenceVO.getWifiName()) || TextUtils.isEmpty(teacherAttenceVO.getWifiName())) {
            UIUtil.showToast(this, "暂未获取考勤信息,请后重试...");
            return;
        }
        UIUtil.showProgress(this, new UIUtil.ProgressCallBack() {
            @Override
            public void callback(final ShapeLoadingDialog dialog) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog!=null){
                            dialog.dismiss();
                            Gson gson = GsonBuilderUtil.create();
                            Intent intent = new Intent(StudentPreAttenceActivity.this, StudentMainActivity.class);
                            intent.putExtra("student_teacherAttence",gson.toJson(teacherAttenceVO));
                            startActivity(intent);
                        }
                    }
                }, 2000);//2秒后执行Runnable中的run方法
            }
        });
    }


    private void selectCourse() {
        Map<String, String> reqData = new HashMap<>();
        reqData.put("classId",String.valueOf(mStudent.getClassId()));
        NetWorkUtil.post(Constant.URL_LOGIN_STUDENT_COURSE, reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result,Gson gson) {
                List<Course> courses = gson.fromJson(result, new TypeToken<List<Course>>() {
                }.getType());
                if(courses==null || courses.size()==0){
                    Toast.makeText(x.app(), "未查到课程信息,请稍后重试", Toast.LENGTH_LONG).show();
                    return;
                }
                final String[] s = new String[courses.size()];
                for(int i=0; i!=courses.size(); i++){
                    Course c = courses.get(i);
                    s[i] = c.getId()+"-"+c.getName();
                }
                new AlertView("请选择 编号-课程", null, "取消",null, s, StudentPreAttenceActivity.this, AlertView.Style.ActionSheet, new OnItemClickListener(){

                    @Override
                    public void onItemClick(Object o, int position) {
                        if(position>=0 && position<s.length){
                            mTvCourseName.setText(s[position]);
                        }

                    }
                }).setCancelable(true).show();
            }
        });

    }

    private void confirm() {
        String courseIdAndName = mTvCourseName.getText().toString();
        if(TextUtils.isEmpty(courseIdAndName) || !courseIdAndName.contains("-")){
            UIUtil.showToast(this,"请先选择课程");
            return;
        }
        checkAttence();
    }


    /**
     * 检测当前是否有有效考勤
     */
    private void checkAttence(){
        HashMap<String, String> reqData = new HashMap<>();
        reqData.put("studentId",String.valueOf(mStudent.getStuId()));
        reqData.put("courseId",mTvCourseName.getText().toString().split("-")[0]);
        NetWorkUtil.post(Constant.URL_STUDENT_QUERY_CHECK_ATTENCE,reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result,Gson gson) {
                StudentAttence studentAttence = gson.fromJson(result,StudentAttence.class);
                Long state = studentAttence.getState();
                if(state==1){
                    String addr = mTvMyLocaltionValue.getText().toString().trim();
                    if(TextUtils.isEmpty(addr) || "查看详情".equals(addr)){
                        UIUtil.alert(StudentPreAttenceActivity.this, "您还未选择定位","是否进入下一步", new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                                getAttence();
                            }
                        });
                        return;
                    }
                    getAttence();
                }else if(state==2){
                    UIUtil.okNoCancel(StudentPreAttenceActivity.this, "请勿重复打卡","已经打过卡了\n课程编号-名称:"+mTvCourseName.getText().toString(), new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                }else if(state==3){
                    UIUtil.alert(StudentPreAttenceActivity.this, "无法打卡","未在规定时间内打卡,已视为缺勤\n课程编号-名称:"+mTvCourseName.getText().toString(), new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                }
            }
        });

    }

    /**
     * 获取考勤信息
     */
    private void getAttence() {
        HashMap<String, String> reqData = new HashMap<>();
        reqData.put("studentId",String.valueOf(mStudent.getStuId()));
        reqData.put("courseId",mTvCourseName.getText().toString().split("-")[0]);
        NetWorkUtil.post(Constant.URL_LOGIN_STUDENT_CALL_ATTENCE,reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result,Gson gson) {
                final TeacherAttenceVO teacherAttenceVO = gson.fromJson(result,TeacherAttenceVO.class);
                if(teacherAttenceVO==null || teacherAttenceVO.getId()==null) {
                    Toast.makeText(x.app(), "获取教师考勤明细失败", Toast.LENGTH_LONG).show();
                    return;
                }
                if(teacherAttenceVO.getState()==2){
                    UIUtil.alert(StudentPreAttenceActivity.this, "无法打卡","当前考勤已经结束", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                    return;
                }
                UIUtil.alert(StudentPreAttenceActivity.this, "请确认是否开始考勤", "教师:" + teacherAttenceVO.getTeacherName() + ",热点名:" + teacherAttenceVO.getWifiName(), new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        saveAttence(teacherAttenceVO);
                    }
                });
            }
        });

    }


}
