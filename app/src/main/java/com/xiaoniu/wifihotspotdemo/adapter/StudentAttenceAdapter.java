package com.xiaoniu.wifihotspotdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.xiaoniu.wifihotspotdemo.R;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;

import java.util.List;
import java.util.Random;

/**
 * Created by think on 2017/5/7 10:14
 * 按照教师考勤维度展示学生考勤信息列表
 */

public class StudentAttenceAdapter extends BaseAdapter {
    private ImageView mIvNumber;
    private TextView mTvStudentName;
    private TextView mTvAttenceState;
    private TextView mTvStudentId;
    private List<StudentAttence> mStudentAttences;
    private Context ctx;
    StudentAttenceAdapter(List<StudentAttence> mStudentAttences,Context ctx){
        this.mStudentAttences = mStudentAttences;
        this.ctx = ctx;
    }


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
        View view = View.inflate(ctx, R.layout.item_teacher_attence_student, null);
        StudentAttence studentAttence = mStudentAttences.get(position);
        mIvNumber = (ImageView)view.findViewById(R.id.iv_number);
        mTvStudentName = (TextView) view.findViewById(R.id.tv_student_name);
        mTvAttenceState = (TextView) view.findViewById(R.id.tv_attence_state);
        mTvStudentId = (TextView) view.findViewById(R.id.tv_student_id);
        Long state = studentAttence.getState();
        if(state==1){
            mTvAttenceState.setText("考勤状态:未打卡");
        }
        if(state==2){
            mTvAttenceState.setText("考勤状态:已出勤");
        }
        if(state==3){
            mTvAttenceState.setText("考勤状态:缺勤");
            mTvAttenceState.setTextColor(Color.RED);
        }
        mTvStudentId.setText("学号:"+studentAttence.getStudentId());
        mTvStudentName.setText("姓名:"+studentAttence.getName());
        Random random = new Random();
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(4).endConfig()
                .buildRoundRect((position+1)+"", Color.rgb(100+random.nextInt(50),100+random.nextInt(50),100+random.nextInt(50)), 10);
        mIvNumber.setImageDrawable(drawable);
        return view;
    }

}