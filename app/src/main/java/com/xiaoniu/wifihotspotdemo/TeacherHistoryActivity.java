package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.domain.TeacherAttenceVO;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.List;


public class TeacherHistoryActivity extends BaseActivity implements View.OnClickListener {
    private TextView mTvBack;
    private ListView mLvTeacherHistory;
    private List<TeacherAttenceVO> teacherAttenceVOs;
    private MyAdapter mMyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_history);
        initView();
    }

    private void initView() {
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mLvTeacherHistory = (ListView) findViewById(R.id.lv_teacher_history);
        mTvBack.setOnClickListener(this);
        queryTeacherHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryTeacherHistory();
    }

    /**
     * 查看详细考勤信息
     * @param attenceId
     */

    private void detail(String attenceId,boolean isRun) {
        Intent intent = new Intent(this,TeacherAttenceDetailActivity.class);
        intent.putExtra("teacherAttenceId",attenceId);
        intent.putExtra("isRun",isRun);
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_back :
                finish();
                break;
        }
    }

    class MyAdapter extends BaseAdapter {

        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        @Override
        public int getCount() {
            return teacherAttenceVOs.size();
        }

        @Override
        public Object getItem(int position) {
            return teacherAttenceVOs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(),R.layout.item_teacher_history, null);
            final TeacherAttenceVO teacherAttenceVO = teacherAttenceVOs.get(position);
            TextView mTvState = (TextView) view.findViewById(R.id.tv_state);
            TextView mTvClass = (TextView) view.findViewById(R.id.tv_class);
            TextView mTvCousre = (TextView) view.findViewById(R.id.tv_cousre);
            TextView mTvAttenceDatetime = (TextView) view.findViewById(R.id.tv_attence_datetime);
            final Long state = teacherAttenceVO.getState();
            if(state==1){
                mTvState.setText("正在进行");
                mTvState.setTextColor(Color.RED);
            }
            if(state==2){
                mTvState.setText("已经结束");
            }
            //设置课程
            mTvCousre.setText(teacherAttenceVO.getCourseName());
            mTvClass.setText(+teacherAttenceVO.getClassId()+"");

            try {
                String start = sdf.format(teacherAttenceVO.getStartTime());
                if(teacherAttenceVO.getEndTime()!=null){
                    String end = sdfTime.format(teacherAttenceVO.getEndTime());
                    mTvAttenceDatetime.setText(start+"-"+end);
                }else{
                    mTvAttenceDatetime.setText(start+"-"+"至今");
                }

            } catch (Exception e) {
                e.printStackTrace();
                UIUtil.showToast(getApplicationContext(),"考勤日期获取失败");
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(state==1){
                        detail(""+teacherAttenceVO.getId(),true);
                        return;
                    }
                    detail(""+teacherAttenceVO.getId(),false);
                }
            });

            return view;
        }

    }


    private void queryTeacherHistory() {
        String json = PrefUtils.getString(this, "teacher", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"当前登录信息失效,请重试");
            return;
        }
        final Gson gson = GsonBuilderUtil.create();
        Teacher teacher = gson.fromJson(json, Teacher.class);
        if(teacher==null || teacher.getId()==null){
            UIUtil.showToast(this,"当前登录信息失效,请重试");
            return;
        }
        RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER_HISTORY);
        params.addQueryStringParameter("teacherId", teacher.getTeacherId()+"");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","通过工号获取教师考勤信息:"+result);
                if(TextUtils.isEmpty(result)){
                    Toast.makeText(x.app(), "通过工号获取教师考勤信息为空", Toast.LENGTH_LONG).show();
                    return;
                }
                teacherAttenceVOs = gson.fromJson(result, new TypeToken<List<TeacherAttenceVO>>() {
                }.getType());
                if(teacherAttenceVOs==null || teacherAttenceVOs.size()==0){
                    Toast.makeText(x.app(), "未查到教师考勤信息", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    if(mMyAdapter!=null){
                        mMyAdapter.notifyDataSetChanged();
                    }else{
                        mMyAdapter = new MyAdapter();
                        mLvTeacherHistory.setAdapter(mMyAdapter);
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

}
