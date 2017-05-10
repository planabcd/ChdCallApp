package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttence;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttenceBackUp;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;
import com.xiaoniu.wifihotspotdemo.util.WifiUtil;
import com.xiaoniu.wifihotspotdemo.view.CountdownView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.xiaoniu.wifihotspotdemo.R.id.tv_back;


public class AttenceActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView mLvTeacherAttenceStudent;
    private SimpleDateFormat sdf;
    private List<StudentAttence> mStudentAttences;
    private ImageView mIvNumber;
    private TextView mTvStudentName;
    private TextView mTvAttenceState;
    private TextView mTvStudentId;
    private TextView mTvAttenceDate;
    private MyAdapter mMyAdapter;
    private TextView mTvRefresh;
    private volatile boolean firstVisit = true;

    private TextView mTvCancel;
    private TextView mTvBack;
    private Teacher mTeacher;
    private volatile Boolean isEnd = false;

    private TeacherAttenceBackUp mTeacherAttenceBackUp;
    private CountdownView mCvCountdownViewTest2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attence);
        initView();
        queryStudentAttence();

    }
    private void initView() {
        String json = PrefUtils.getString(this, "teacher", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"登录失效,请重新登录");
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        mTeacher = gson.fromJson(json, Teacher.class);
        String backUpStr = PrefUtils.getString(this, "teacherAttence_backup_" + mTeacher.getTeacherId(), null);
        if(TextUtils.isEmpty(backUpStr)){
            UIUtil.showToast(this,"获取考勤信息失败,请稍后重试");
            return;
        }
        mTeacherAttenceBackUp = gson.fromJson(backUpStr, TeacherAttenceBackUp.class);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mTvBack = (TextView) findViewById(tv_back);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mLvTeacherAttenceStudent = (ListView) findViewById(R.id.lv_teacher_attence_student);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);

        mTvRefresh.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mTvBack.setOnClickListener(this);

        mLvTeacherAttenceStudent.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                //如果是第一次进入,开启热点
                if(firstVisit){
                    boolean wifiHotspot = WifiUtil.createWifiHotspot(AttenceActivity.this, mTeacherAttenceBackUp.getWifiName(), mTeacherAttenceBackUp.getWifiPwd());
                    if(wifiHotspot){
                        //开始计时
                        mCvCountdownViewTest2 = (CountdownView)findViewById(R.id.cv_countdownViewTest2);

                        //计算倒计时初值
                        long time = (long)1 * 15 * 1000;
                       /* Calendar calendar = Calendar.getInstance();
                        String startTime = mTeacherAttenceBackUp.getStartTime();
                        try {
                            Date startDate = sdf.parse(startTime);
                            calendar.setTime(startDate);
                            calendar.add(Calendar.MINUTE,Constant.ATTENCE_LENGTH);
                            Date endDate = calendar.getTime();
                            if(endDate.getTime()-new Date().getTime()>0){
                                time = endDate.getTime()-startDate.getTime();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }*/
                        mCvCountdownViewTest2.start(time);
                        mCvCountdownViewTest2.customTimeShow(false,false,true,true,true);
                        mCvCountdownViewTest2.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                            @Override
                            public void onEnd(CountdownView cv) {
                                doCancelAction();
                            }
                        });
                        firstVisit = false;
                    }

                }
            }
        });

    }


    /**
     * 终止考勤
     */
    private void doCancelAction(){
        if(isEnd){
            UIUtil.okNoCancel(this, "当前考勤已经终止", "请不要重复终止考勤", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                }
            });
            return;
        }
        if(mCvCountdownViewTest2!=null){
            mCvCountdownViewTest2.stop();
        }
        new SweetAlertDialog(AttenceActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("是否关闭热点")
                .setContentText( "如果关闭热点,考勤将终止,未打卡学生将视为缺勤")
                .setCancelText("取消")
                .setConfirmText("确认")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        if(mCvCountdownViewTest2!=null && mCvCountdownViewTest2.getRemainTime()>0){
                            mCvCountdownViewTest2.start(mCvCountdownViewTest2.getRemainTime());
                        }
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                cancelCall();
            }
        }).show();

    }


    /**
     * 查询学生考勤列表
     */
    private void queryStudentAttence() {
        final Gson gson = GsonBuilderUtil.create();
        RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER_STUDENT_ATTENCE);
        params.addQueryStringParameter("teacherAttenceId", mTeacherAttenceBackUp.getTeacherAttenceId()+"");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","通过教师考勤明细Id获取学生考勤信息:"+result);
                if(TextUtils.isEmpty(result)){
                    Toast.makeText(x.app(), "通过教师考勤明细Id获取学生考勤信息为空", Toast.LENGTH_LONG).show();
                    return;
                }
                List<StudentAttence> studentAttences = gson.fromJson(result, new TypeToken<List<StudentAttence>>() {
                }.getType());
                if(studentAttences==null || studentAttences.size()==0){
                    Toast.makeText(x.app(), "未查到详细学生考勤信息", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    mStudentAttences = studentAttences;
                    if(mMyAdapter!=null){
                        mMyAdapter.notifyDataSetChanged();
                    }else{
                        mMyAdapter = new MyAdapter();
                        mLvTeacherAttenceStudent.setAdapter(mMyAdapter);
                    }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case tv_back:
                if(!isEnd){
                    new SweetAlertDialog(AttenceActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("确认离开")
                            .setContentText("是否终止当前考勤")
                            .setCancelText("取消")
                            .setConfirmText("确认")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                    Intent intent = new Intent(AttenceActivity.this, TeacherPreAttenceActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            doCancelAction();
                        }
                    }).show();
                }else{
                    Intent intent = new Intent(AttenceActivity.this, TeacherPreAttenceActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.tv_refresh:
                UIUtil.showToastS(getApplicationContext(),"正在刷新,稍等一会");
                queryStudentAttence();
                break;
            case R.id.tv_cancel:
                doCancelAction();
                break;
            default:
                break;
        }
    }



    /**
     * 教师端考勤结束
     */
    private void cancelCall() {
        RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER_CANCEL_CALL_ATTENCE);
        params.addQueryStringParameter("tacherAttenceId", mTeacherAttenceBackUp.getTeacherAttenceId()+"");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","教师终止考勤返回信息:"+result);
                if(TextUtils.isEmpty(result)){
                    Toast.makeText(x.app(), "终止考勤失败,请稍后重试", Toast.LENGTH_LONG).show();
                    return;
                }
                Gson gson = GsonBuilderUtil.create();
                TeacherAttence teacherAttence = gson.fromJson(result,TeacherAttence.class);
                if(teacherAttence==null){
                    Toast.makeText(x.app(), "终止考勤失败,请稍后重试", Toast.LENGTH_LONG).show();
                    return;
                }
                //删除缓存
                isEnd = true;
                PrefUtils.setString(AttenceActivity.this, "teacherAttence_backup_" + mTeacher.getTeacherId(), "");
                UIUtil.ok(AttenceActivity.this, "终止考勤成功", "刷新学生考勤数据", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        queryStudentAttence();
                    }
                });

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

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mStudentAttences.size();
        }

        @Override
        public Object getItem(int position) {
            return mStudentAttences.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = View.inflate(AttenceActivity.this,R.layout.item_teacher_attence_student, null);
            StudentAttence studentAttence = mStudentAttences.get(position);
            mIvNumber = (ImageView)view.findViewById(R.id.iv_number);
            mTvStudentName = (TextView) view.findViewById(R.id.tv_student_name);
            mTvAttenceState = (TextView) view.findViewById(R.id.tv_attence_state);
            mTvStudentId = (TextView) view.findViewById(R.id.tv_student_id);
            mTvAttenceDate = (TextView) view.findViewById(R.id.tv_attence_date);

            Long state = studentAttence.getState();
            if(state==1){
                mTvAttenceState.setText("待打卡");
            }
            if(state==2){
                mTvAttenceState.setText("已出勤");
            }
            if(state==3){
                mTvAttenceState.setText("未打卡");
                mTvAttenceState.setTextColor(Color.RED);
            }
            mTvStudentId.setText(studentAttence.getStudentId()+"");
            mTvStudentName.setText(studentAttence.getName());
            Random random = new Random();
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4).endConfig()
                    .buildRoundRect((position+1)+"", Color.rgb(100+random.nextInt(50),100+random.nextInt(50),100+random.nextInt(50)), 10);
            mIvNumber.setImageDrawable(drawable);
            try {
                String format = sdf.format(studentAttence.getCreated());
                mTvAttenceDate.setText(format);
            } catch (Exception e) {
                Log.e("TAG","日期转换失败");
            }
            return view;
        }

    }

}
