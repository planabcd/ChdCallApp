package com.xiaoniu.wifihotspotdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.xiaoniu.wifihotspotdemo.R;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttence;

import java.util.Random;

public class StudentAttenceListAdapter extends ArrayAdapter<StudentAttence> {

    private final LayoutInflater mInflater;
    private int mResource;
    private ImageView mIvNumber;
    private TextView mTvStudentName;
    private TextView mTvAttenceState;
    private TextView mTvStudentId;

    public StudentAttenceListAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }

        StudentAttence studentAttence = getItem(position);

        mIvNumber = (ImageView)convertView.findViewById(R.id.iv_number);
        mTvStudentName = (TextView) convertView.findViewById(R.id.tv_student_name);
        mTvAttenceState = (TextView) convertView.findViewById(R.id.tv_attence_state);
        mTvStudentId = (TextView) convertView.findViewById(R.id.tv_student_id);
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
        return convertView;
    }

}
