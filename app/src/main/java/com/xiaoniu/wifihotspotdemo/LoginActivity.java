package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtLoginInputName;
    private EditText mEtLoginInputPassword;
    private RadioGroup mRgUsertype;
    private Button mBtnLogin;
    private TextView mTvForgotPwd;
    private TextView mTvBind;
   

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
    }

    

    private void initListener() {
        mBtnLogin.setOnClickListener(this);
        mTvForgotPwd.setOnClickListener(this);
        mTvBind.setOnClickListener(this);

    }

    private void initView() {
        mEtLoginInputName = (EditText) findViewById(R.id.et_login_input_name);
        mEtLoginInputPassword = (EditText) findViewById(R.id.et_login_input_password);
        mRgUsertype = (RadioGroup) findViewById(R.id.rg_usertype);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mTvForgotPwd = (TextView) findViewById(R.id.tv_forgot_pwd);
        mTvBind = (TextView) findViewById(R.id.tv_bind);

       
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.tv_forgot_pwd:
                fogotPwd();
                break;
            case R.id.tv_bind:
                bind();
                break;
            default:
                break;
        }
    }

    /**
     * 创建热点
     */
    private void bind() {
        Intent it = new Intent(LoginActivity.this,TestWifiActivity.class);
        startActivity(it);
        finish();
    }

    /**
     * 忘记密码
     */
    private void fogotPwd() {
        Intent it = new Intent(this,VoiceRegisterActivity.class);
        startActivity(it);
    }

    private void login() {
        String name = mEtLoginInputName.getText().toString().trim();
        String pwd = mEtLoginInputPassword.getText().toString().trim();
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)){
            UIUtil.showToast(getApplicationContext(),"请输入用户名或者密码");
            return;
        }
        int checkedRadioButtonId = mRgUsertype.getCheckedRadioButtonId();
        final Gson gson = GsonBuilderUtil.create();
        if(checkedRadioButtonId==R.id.rb_student){

            RequestParams params = new RequestParams(Constant.URL_LOGIN_STUDENT);
            params.addQueryStringParameter("studentId", name);
            params.addQueryStringParameter("pwd", pwd);

            x.http().post(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.i("TAG","登录获取学生信息:"+result);
                    if(TextUtils.isEmpty(result)){
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Student student = gson.fromJson(result, Student.class);
                    if(student!=null && student.getId()!=null){
                        PrefUtils.setString(getApplication(),"student",result);
                        Intent it = new Intent(LoginActivity.this,HomeActivity.class);
                        startActivity(it);
                        finish();
                    }else{
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
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
        }else{
            RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER);
            params.addQueryStringParameter("teacherId", name);
            params.addQueryStringParameter("pwd", pwd);

            x.http().post(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.i("TAG","登录获取教师信息:"+result);
                    if(TextUtils.isEmpty(result)){
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String,Object> map = gson.fromJson(result,new TypeToken<Map<String,Object>>(){}.getType());

                    if(map!=null && map.get("id")!=null){
                        PrefUtils.setString(getApplication(),"teacher",result);
                        Intent it = new Intent(LoginActivity.this,TeacherHomeActivity.class);
                        startActivity(it);
                        finish();
                    }else{
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
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


}
