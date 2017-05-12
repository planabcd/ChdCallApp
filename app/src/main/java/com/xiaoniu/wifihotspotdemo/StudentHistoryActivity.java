package com.xiaoniu.wifihotspotdemo;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.domain.StudentAttenceVO;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StudentHistoryActivity extends AppCompatActivity implements View.OnClickListener,OnItemClickListener, OnDismissListener {
    private TextView mTvBack;
    private ListView mLvStudentHistory;
    private List<StudentAttenceVO> studentAttenceVOs;
    private MyAdapter mMyAdapter;

    private AlertView mAlertViewExt;//窗口拓展例子
    private EditText etName;//拓展View内容
    private InputMethodManager imm;
    private String spId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_history);
        initView();
    }

    private void initView() {
        initInputDialog();
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mLvStudentHistory = (ListView) findViewById(R.id.lv_student_history);
        mTvBack.setOnClickListener(this);
        queryStudentHistory();
    }

    /**
     * 初始化输入框
     */
    private void initInputDialog() {
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //拓展窗口
        mAlertViewExt = new AlertView("提示", "请填写考勤异常理由！", "取消", null, new String[]{"完成"}, this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alertext_form,null);
        etName = (EditText) extView.findViewById(R.id.etName);
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen=imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen&&focus ? 120 :0);
                System.out.println(isOpen);
            }
        });
        mAlertViewExt.addExtView(extView);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_back :
                finish();
                break;
        }
    }

    /**
     * 输入框取消时触发
     * @param o
     */
    @Override
    public void onDismiss(Object o) {

    }


    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if(o == mAlertViewExt && position != AlertView.CANCELPOSITION){
            String remark = etName.getText().toString();
            if(remark.isEmpty()){
                Toast.makeText(this, "啥都没填呢", Toast.LENGTH_SHORT).show();
            }
            else{
                specialCall(remark);
            }
            return;
        }
    }


    /**
     * 关闭软键盘
     */
    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(),0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }



    class MyAdapter extends BaseAdapter {

        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @Override
        public int getCount() {
            return studentAttenceVOs.size();
        }

        @Override
        public Object getItem(int position) {
            return studentAttenceVOs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(),R.layout.item_student_history, null);
            final StudentAttenceVO studentAttenceVO = studentAttenceVOs.get(position);

            final TextView mTvAttenceState = (TextView) view.findViewById(R.id.tv_attence_state);
            TextView mTvCourse = (TextView) view.findViewById(R.id.tv_course);
            TextView mTvTeacher = (TextView) view.findViewById(R.id.tv_teacher);
            TextView mTvAttenceDatetime = (TextView) view.findViewById(R.id.tv_attence_datetime);

            final Long state = studentAttenceVO.getState();
            String remark = studentAttenceVO.getRemark();
            if(state==1){
                mTvAttenceState.setText("待打卡");
            }
            if(state==2){
                mTvAttenceState.setText("已出勤");
            }
            if(state==3){
                if(!TextUtils.isEmpty(remark)){
                    mTvAttenceState.setText("待审批");
                    mTvAttenceState.setTextColor(Color.rgb(128,146,143));
                }else{
                    mTvAttenceState.setText("已缺勤");
                    mTvAttenceState.setTextColor(Color.RED);
                }
            }
            mTvTeacher.setText(studentAttenceVO.getTeacherName());
            //设置课程
            mTvCourse.setText(studentAttenceVO.getCourseName());
            try {
                String format = sdf.format(studentAttenceVO.getUpdated());
                mTvAttenceDatetime.setText("考勤时间:"+format);
            } catch (Exception e) {
                e.printStackTrace();

                UIUtil.showToast(getApplicationContext(),"考勤日期获取失败");
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(state==3){
                        String desc = mTvAttenceState.getText().toString();
                        if(!TextUtils.isEmpty(desc) && "待审批".equals(desc)){
                            UIUtil.okTips(StudentHistoryActivity.this,"正在审批","请勿重复提交");
                            return;
                        }
                        UIUtil.alert(StudentHistoryActivity.this, "请确认","是否提报考勤异常\n课程:"+studentAttenceVO.getCourseName(), new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                                mAlertViewExt.show();
                                spId = studentAttenceVO.getId()+"";
                            }
                        });
                    }else if(state==2){
                        UIUtil.okTips(StudentHistoryActivity.this,"考勤正常","无需提报考勤异常");
                    }else if(state==1){
                        UIUtil.ok(StudentHistoryActivity.this, "请确认", "是否进入考勤", new UIUtil.AlterCallBack() {
                            @Override
                            public void confirm() {
                                Intent it = new Intent(StudentHistoryActivity.this,StudentPreAttenceActivity.class);
                                startActivity(it);
                            }
                        });
                        
                    }

                }
            });
            return view;
        }

    }

    /**
     * 考勤异常申请
     */
    private void specialCall(String remark) {
        Map<String,String> params = new HashMap<String,String>();
        params.put("studentAttenceId",spId);
        try {
            String encode = URLEncoder.encode(remark, "UTF-8");
            params.put("remark",encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("STU","异常考勤申请理由编码错误");
            UIUtil.alert(StudentHistoryActivity.this, "上传考勤信息错误","请稍后重试", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                }
            });
            return;
        }
        NetWorkUtil.postYN(Constant.URL_STUDENT_EXCEPTION_CALL, params, new NetWorkUtil.WorkerYN() {
            @Override
            public void success(String result, Gson gson) {
                if(TextUtils.isEmpty(result) || result.length()<3){
                    UIUtil.okTips(StudentHistoryActivity.this,"请确认","未找到异常考勤或者已经申请过异常考勤");
                    return;
                }
                UIUtil.okTips(StudentHistoryActivity.this,"请确认","异常考勤已经提交");
            }
        });
    }


    private void queryStudentHistory() {
        String json = PrefUtils.getString(this, "student", null);
        if(TextUtils.isEmpty(json)){
            UIUtil.showToast(this,"当前登录信息失效,请重试");
            return;
        }
        final Gson gson = GsonBuilderUtil.create();
        Student student = gson.fromJson(json, Student.class);
        if(student==null || student.getId()==null){
            UIUtil.showToast(this,"当前登录信息失效,请重试");
            return;
        }
        RequestParams params = new RequestParams(Constant.URL_LOGIN_STUDENT_HISTORY);
        params.addQueryStringParameter("studentId", student.getStuId()+"");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("TAG","通过学号获取学生考勤信息:"+result);
                if(TextUtils.isEmpty(result)){
                    Toast.makeText(x.app(), "通过学号获取学生考勤信息为空", Toast.LENGTH_LONG).show();
                    return;
                }
                studentAttenceVOs = gson.fromJson(result, new TypeToken<List<StudentAttenceVO>>() {
                }.getType());
                if(studentAttenceVOs==null || studentAttenceVOs.size()==0){
                    Toast.makeText(x.app(), "未查到学生考勤信息", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    if(mMyAdapter!=null){
                        mMyAdapter.notifyDataSetChanged();
                    }else{
                        mMyAdapter = new MyAdapter();
                        mLvStudentHistory.setAdapter(mMyAdapter);
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
