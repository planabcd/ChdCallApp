package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.ClassInfo;
import com.xiaoniu.wifihotspotdemo.domain.Course;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttence;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttenceBackUp;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xiaoniu.wifihotspotdemo.R.id.tv_back;


public class TeacherPreAttenceActivity extends FragmentActivity implements View.OnClickListener {
    private TextView mEtClassId;
    private TextView mEtCourseName;
    private TextView mEtStartTime;
    private TextView mTvConfirm;

    private RelativeLayout mRlSelectClassId;
    private RelativeLayout mRlSelectCourseId;
    private TextView mTvTeacherInfo;

    private TextView mTvBack;

    private Teacher mTeacher;
    private TeacherAttenceBackUp teacherAttenceBackUp;
    private SimpleDateFormat sdf;
    private volatile Boolean isCheckBackUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_pre_attence);
        initView();
    }

    private void initView() {
        mEtClassId = (TextView) findViewById(R.id.et_class_id);
        mEtCourseName = (TextView) findViewById(R.id.et_course_name);
        mEtStartTime = (TextView) findViewById(R.id.et_start_time);
        mRlSelectClassId = (RelativeLayout) findViewById(R.id.rl_select_class_id);
        mRlSelectCourseId = (RelativeLayout) findViewById(R.id.rl_select_course_id);
        mTvTeacherInfo = (TextView) findViewById(R.id.tv_teacher_info);
        mTvBack = (TextView) findViewById(tv_back);
        mTvConfirm = (TextView) findViewById(R.id.tv_confirm);

        mRlSelectCourseId.setOnClickListener(this);
        mRlSelectClassId.setOnClickListener(this);
        mTvBack.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);

        String json = PrefUtils.getString(this, "teacher", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"登录失效,请重新登录");
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mTeacher = gson.fromJson(json, Teacher.class);
        mTvTeacherInfo.setText(mTeacher.getTeacherId()+"-"+mTeacher.getName());
        checkBackUp();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                getAttence();
                break;
            case R.id.rl_select_class_id:
                slectClass();
                break;
            case R.id.rl_select_course_id:
                selectCourse();
                break;
            case R.id.tv_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 进入考勤页面
     */
    private void openWifiHot() {
        Intent it = new Intent(TeacherPreAttenceActivity.this, AttenceActivity.class);
        startActivity(it);
        finish();
    }

    private void newAttence(String classIdAndName,String courseIdAndName){
        String classId = classIdAndName.split("-")[0];
        String courseId = courseIdAndName.split("-")[0];
        Map<String,String> reqData = new HashMap<String,String>();
        reqData.put("teacherId",String.valueOf(mTeacher.getTeacherId()));
        reqData.put("classId",classId);
        reqData.put("courseId",courseId);

        NetWorkUtil.post(Constant.URL_LOGIN_TEACHER_CALL_ATTENCE, reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(final String result, final Gson gson) {
                Log.i("TAG","生成教师考勤明细:"+result);
                final TeacherAttence teacherAttence = gson.fromJson(result, TeacherAttence.class);
                if(teacherAttence==null || teacherAttence.getId()==null) {
                    Toast.makeText(x.app(), "生成教师考勤明细失败", Toast.LENGTH_LONG).show();
                    return;
                }
                //暂存教师端开启考勤预配置信息
                TeacherAttenceBackUp backUp = new TeacherAttenceBackUp();
                backUp.setClassIdAndClassName(mEtClassId.getText().toString());
                backUp.setCourseIdAndCourseName(mEtCourseName.getText().toString());
                backUp.setStartTime(mEtStartTime.getText().toString());
                backUp.setTeacherIdAndName(mTvTeacherInfo.getText().toString());
                backUp.setWifiName(teacherAttence.getWifiName());
                backUp.setWifiPwd(teacherAttence.getWifiPwd());
                backUp.setTeacherAttenceId(teacherAttence.getId());
                String backUpStr = gson.toJson(backUp);
                PrefUtils.setString(TeacherPreAttenceActivity.this,"teacherAttence_backup_"+mTeacher.getTeacherId(),backUpStr);
                UIUtil.okNoCancel(TeacherPreAttenceActivity.this, "确认开始考勤", "SSID:" + teacherAttence.getWifiName() + "\nPWD:" + teacherAttence.getWifiPwd(), new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        openWifiHot();
                    }
                });
            }
        });
    }


    /**
     * 生成教师考勤信息
     */
    private void getAttence() {
        if(isCheckBackUp){
            checkBackUp();
            return;
        }
        final String classIdAndName = mEtClassId.getText().toString().trim();
        final String courseIdAndName = mEtCourseName.getText().toString().trim();
        if(TextUtils.isEmpty(courseIdAndName) || TextUtils.isEmpty(classIdAndName)){
            UIUtil.showToast(this,"请选择课程或者班级");
            return;
        }
        UIUtil.alert(this, "是否生成考勤信息", "点击确定生成后只可以终止不可以删除", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                //没有缓存信息,直接生成新的考勤信息
                newAttence(classIdAndName,courseIdAndName);
            }
        });
    }

    /**
     * 根据预配置缓存判断是否终止当前考勤
     */
    private void checkBackUp(){
        String backUpStr = PrefUtils.getString(this, "teacherAttence_backup_" + mTeacher.getTeacherId(), null);
        //如果有缓存的预配置信息,直接读取
        if(!TextUtils.isEmpty(backUpStr)){
            Gson gson = GsonBuilderUtil.create();
            teacherAttenceBackUp = gson.fromJson(backUpStr, TeacherAttenceBackUp.class);
            mEtStartTime.setText(teacherAttenceBackUp.getStartTime());
            mEtCourseName.setText(teacherAttenceBackUp.getCourseIdAndCourseName());
            mEtClassId.setText(teacherAttenceBackUp.getClassIdAndClassName());
            String startTime = teacherAttenceBackUp.getStartTime();
            try {
                Date startDate = sdf.parse(startTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.MINUTE,Constant.ATTENCE_LENGTH);
                Date endDate = calendar.getTime();
                if(endDate.getTime()>new Date().getTime()){
                    //如果当前时间小于点名的结束时间
                    //进入当前正在进行的考勤
                    UIUtil.ok(TeacherPreAttenceActivity.this, "是否进入", "当前有正在进行的考勤\nSSID:"+teacherAttenceBackUp.getWifiName()+"\nPWD:"+teacherAttenceBackUp.getWifiPwd(), new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                            openWifiHot();
                        }
                    });
                }else{
                    UIUtil.ok(TeacherPreAttenceActivity.this, "是否终止", "当前有已经过期的考勤,但是还未手动终止\nSSID:"+teacherAttenceBackUp.getWifiName()+"\nPWD:"+teacherAttenceBackUp.getWifiPwd(), new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                            cancelCall();
                        }
                    });
                }
            } catch (ParseException e) {
                e.printStackTrace();
                UIUtil.showToast(this,"日期转换出错");
            }
        }else{
            isCheckBackUp = false;
            try {
                String format = sdf.format(new Date());
                mEtStartTime.setText(format);
            } catch (Exception e) {
                Log.e("TAG","日期转换失败");
            }
        }
    }

    /**
     * 终止当前缓存的考勤
     */
    private void cancelCall(){
        Map<String,String> reqData = new HashMap<String,String>();
        reqData.put("tacherAttenceId",String.valueOf(teacherAttenceBackUp.getTeacherAttenceId()));
        NetWorkUtil.post(Constant.URL_LOGIN_TEACHER_CANCEL_CALL_ATTENCE, reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result, Gson gson) {
                Log.i("TAG","教师终止考勤返回信息:"+result);
                TeacherAttence teacherAttence = gson.fromJson(result,TeacherAttence.class);
                if(teacherAttence==null){
                    Toast.makeText(x.app(), "教师终止考勤失败,请稍后重试", Toast.LENGTH_LONG).show();
                    return;
                }
                //清空缓存
                PrefUtils.setString(TeacherPreAttenceActivity.this,"teacherAttence_backup_" + mTeacher.getTeacherId(),"");
                isCheckBackUp = false;
                try {
                    String format = sdf.format(new Date());
                    mEtCourseName.setText("");
                    mEtClassId.setText("");
                    mEtStartTime.setText(format);
                } catch (Exception e) {
                    Log.e("TAG","日期转换失败");
                }
                UIUtil.okNoCancel(TeacherPreAttenceActivity.this, "终止考勤成功", "请选择班级课程信息重新生成考勤信息", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {

                    }
                });

            }
        });
    }


    /**
     * 选择课程
     */
    private void selectCourse() {
        RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER_COURSE);
        params.addQueryStringParameter("teacherId", mTeacher.getTeacherId()+"");
        final Gson gson = GsonBuilderUtil.create();
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","通过教师工号获取课程信息:"+result);
                if(TextUtils.isEmpty(result)){
                    Toast.makeText(x.app(), "通过教师工号获取课程信息为空", Toast.LENGTH_LONG).show();
                    return;
                }
                List<Course> courses = gson.fromJson(result, new TypeToken<List<Course>>() {
                }.getType());
                if(courses==null || courses.size()==0){
                    Toast.makeText(x.app(), "未查到课程信息", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    final String[] s = new String[courses.size()];
                    for(int i=0; i!=courses.size(); i++){
                        Course c = courses.get(i);
                        s[i] = c.getId()+"-"+c.getName();
                    }
                    new AlertView("请选择课程", null, "取消",null, s, TeacherPreAttenceActivity.this, AlertView.Style.ActionSheet, new OnItemClickListener(){

                        @Override
                        public void onItemClick(Object o, int position) {
                            if(position>=0 && position<s.length){
                                mEtCourseName.setText(s[position]);
                            }
                        }
                    }).setCancelable(true).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), "服务器繁忙,请稍后再试", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    /**
     * 选择班级
     */
    private void slectClass() {
        String courseIdAndName = mEtCourseName.getText().toString();
        if(TextUtils.isEmpty(courseIdAndName) || !courseIdAndName.contains("-")){
            UIUtil.showToast(this,"请先选择课程");
            return;
        }
        Map<String,String> reqData = new HashMap<String,String>();
        reqData.put("courseId",courseIdAndName.split("-")[0]);
        NetWorkUtil.post(Constant.URL_CLASS_QUERY_BY_COURSE_ID, reqData, new NetWorkUtil.Worker() {
            @Override
            public void success(String result, Gson gson) {
                Log.i("TAG", "通过教师Id获取班级信息:" + result);
                List<ClassInfo> classes = gson.fromJson(result, new TypeToken<List<ClassInfo>>() {
                }.getType());
                if (classes == null || classes.size() == 0) {
                    Toast.makeText(x.app(), "未查到班级信息", Toast.LENGTH_LONG).show();
                    return;
                }
                final String[] s = new String[classes.size()];
                for (int i = 0; i != classes.size(); i++) {
                    ClassInfo c = classes.get(i);
                    s[i] = c.getClassId() + "-" + c.getMarjor();
                }
                new AlertView("请选择班级", null, "取消", null, s, TeacherPreAttenceActivity.this, AlertView.Style.ActionSheet, new OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position >= 0 && position < s.length) {
                            mEtClassId.setText(s[position]);
                        }
                    }
                }).setCancelable(true).show();
            }
        });
    }


}
