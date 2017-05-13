package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class TeacherAttenceDetailActivity extends BaseActivity implements View.OnClickListener{

    private TextView mTvBack;
    private TextView mTvRefresh;
    private ListView mLvTeacherAttenceStudent;
    private Teacher mTeacher;
    private TextView mTvCancel;
    private String mTeacherAttenceId;
    private List<StudentAttence> mStudentAttences;
    private MyAdapter mMyAdapter;
    private ImageView mIvNumber;
    private TextView mTvStudentName;
    private TextView mTvAttenceState;
    private TextView mTvStudentId;
    private TextView mTvAttenceDate;
    private SimpleDateFormat sdf;
    private volatile boolean isEnd = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attence_detail);
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

        mTvBack = (TextView) findViewById(R.id.tv_back);
        mTvRefresh = (TextView) findViewById(R.id.tv_refresh);
        mLvTeacherAttenceStudent = (ListView) findViewById(R.id.lv_teacher_attence_student);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvRefresh.setOnClickListener(this);
        mTvBack.setOnClickListener(this);
        Intent intent = getIntent();
        mTeacherAttenceId = intent.getStringExtra("teacherAttenceId");
        boolean isRun = intent.getBooleanExtra("isRun",false);
        if(isRun){
            mTvCancel.setVisibility(View.VISIBLE);
            mTvCancel.setOnClickListener(this);
        }else{
            mTvCancel.setVisibility(View.GONE);
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        queryStudentAttence();
    }

    /**
     *  查询学生考勤列表
     */
    private void queryStudentAttence() {
        Map<String,String> params = new HashMap<String,String>();
        params.put("teacherAttenceId",mTeacherAttenceId);
        NetWorkUtil.post(Constant.URL_LOGIN_TEACHER_STUDENT_ATTENCE, params, new NetWorkUtil.Worker() {
            public void success(String result, Gson gson) {
                mStudentAttences = gson.fromJson(result, new TypeToken<List<StudentAttence>>() {
                }.getType());
                if(mMyAdapter!=null){
                    mMyAdapter.notifyDataSetChanged();
                }else{
                    mMyAdapter = new MyAdapter();
                    mLvTeacherAttenceStudent.setAdapter(mMyAdapter);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_cancel:
                if(!isEnd){
                    cancel();
                }else{
                    UIUtil.okNoCancel(this, "当前考勤已经结束", "请不要重复操作", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                }
                break;
            case R.id.tv_refresh:
                UIUtil.okNoCancel(this, "正在刷新", "请稍等片刻", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        queryStudentAttence();
                    }
                });

                break;
            default:
                break;
        }
    }

    /**
     * 结束考勤
     */
    private void cancel() {
        UIUtil.alert(this, "确认结束考勤", "结束后不可以撤销", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER_CANCEL_CALL_ATTENCE);
                params.addQueryStringParameter("tacherAttenceId", mTeacherAttenceId);
                x.http().post(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.i("TAG","教师终止考勤返回信息:"+result);
                        if(TextUtils.isEmpty(result) || result.length()<3){
                            Toast.makeText(x.app(), "终止考勤失败,请稍后重试", Toast.LENGTH_LONG).show();
                            return;
                        }
                        isEnd = true;
                        Gson gson = GsonBuilderUtil.create();
                        //删除缓存
                        PrefUtils.setString(TeacherAttenceDetailActivity.this, "teacherAttence_backup_" + mTeacher.getTeacherId(), "");
                        UIUtil.ok(TeacherAttenceDetailActivity.this, "终止考勤成功", "刷新学生考勤数据", new UIUtil.AlterCallBack() {
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
            View view = View.inflate(TeacherAttenceDetailActivity.this,R.layout.item_teacher_attence_student, null);
            final StudentAttence studentAttence = mStudentAttences.get(position);
            mIvNumber = (ImageView)view.findViewById(R.id.iv_number);
            mTvStudentName = (TextView) view.findViewById(R.id.tv_student_name);
            mTvAttenceState = (TextView) view.findViewById(R.id.tv_attence_state);
            mTvStudentId = (TextView) view.findViewById(R.id.tv_student_id);
            mTvAttenceDate = (TextView) view.findViewById(R.id.tv_attence_date);
            final String remark = studentAttence.getRemark();
            Long state = studentAttence.getState();
            if(state==1){
                mTvAttenceState.setText("待打卡");
            }
            if(state==2){
                String netRemark = studentAttence.getRemark();
                if(TextUtils.isEmpty(netRemark)){
                    mTvAttenceState.setText("已出勤");
                }else{
                    mTvAttenceState.setText("已审批");
                }
            }
            if(state==3){
                if(!TextUtils.isEmpty(remark)){
                    //有考勤异常审批
                    mTvAttenceState.setText("待审批");
                    mTvAttenceState.setTextColor(Color.rgb(128,146,143));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UIUtil.alert(TeacherAttenceDetailActivity.this, "请确认审批","如果确认,该学生的考勤状态将变为已打卡\n申请理由:"+remark, new UIUtil.AlterCallBack() {
                                @Override
                                public void confirm() {
                                    Map<String,String> params = new HashMap<String,String>();
                                    params.put("studentAttenceId",studentAttence.getId()+"");
                                    NetWorkUtil.post(Constant.URL_TEACHER_SPECIAL_CALL, params, new NetWorkUtil.Worker() {
                                        @Override
                                        public void success(String result, Gson gson) {
                                            UIUtil.okTips(TeacherAttenceDetailActivity.this,"审批成功","学生姓名:"+studentAttence.getName());
                                            queryStudentAttence();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else{
                    mTvAttenceState.setText("未打卡");
                    mTvAttenceState.setTextColor(Color.RED);
                }
            }
            mTvStudentId.setText(""+studentAttence.getStudentId());
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
