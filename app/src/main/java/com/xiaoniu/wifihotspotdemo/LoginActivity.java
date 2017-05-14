package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.common.Constant;
import com.xiaoniu.wifihotspotdemo.domain.Student;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;
import com.xiaoniu.wifihotspotdemo.util.NetWorkUtil;
import com.xiaoniu.wifihotspotdemo.util.PrefUtils;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtLoginInputName;
    private EditText mEtLoginInputPassword;
    private RadioGroup mRgUsertype;
    private Button mBtnLogin;
    private TextView mTvForgotPwd;
    private TextView mTvBind;
    private ImageView mIvVoice;
    private int LOGIN_TYPE;

    private final int LOGIN = 0;
    private final int BIND_MAC_ADDRESS = 1;
    private final int BIND_VOICE = 2;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;
    private Toast mToast;
    //用户唯一标示
    private String mAuId;

    //文本声纹密码类型
    private static final int PWD_TYPE_TEXT = 1;
    //文本声纹密码
    private String mTextPwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
        // 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
        mVerifier = SpeakerVerifier.createVerifier(this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
    }


    private void initListener() {
        mBtnLogin.setOnClickListener(this);
        mTvForgotPwd.setOnClickListener(this);
        mTvBind.setOnClickListener(this);
        mIvVoice.setOnClickListener(this);

    }

    private void initView() {
        mEtLoginInputName = (EditText) findViewById(R.id.et_login_input_name);
        mEtLoginInputPassword = (EditText) findViewById(R.id.et_login_input_password);
        mRgUsertype = (RadioGroup) findViewById(R.id.rg_usertype);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mTvForgotPwd = (TextView) findViewById(R.id.tv_forgot_pwd);
        mTvBind = (TextView) findViewById(R.id.tv_bind);
        mIvVoice = (ImageView) findViewById(R.id.iv_voice);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                LOGIN_TYPE = LOGIN;
                login();
                break;
            case R.id.tv_forgot_pwd:
                fogotPwd();
                break;
            case R.id.tv_bind:
                bind();
                break;
            case R.id.iv_voice:
                loginVoice();
                break;
            default:
                break;
        }
    }

    /**
     * 声纹登录
     */
    private void loginVoice() {
        if (mRgUsertype.getCheckedRadioButtonId() == R.id.rb_teacher) {
            showTip("目前声纹录入只支持学生端");
            return;
        }
        String name = mEtLoginInputName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showTip("请输入学号");
            return;
        }
        mAuId = "s" + name;
        Map<String, String> params = new HashMap<>();
        params.put("studentId", name);
        NetWorkUtil.postYN(Constant.URL_LOGIN_STUDENT_CHECK, params, new NetWorkUtil.WorkerYN() {
            @Override
            public void success(String result, Gson gson) {
                if (TextUtils.isEmpty(result) || result.length() < 3) {
                    showTip("用户名错误,请重新输入");
                    mEtLoginInputName.setText("");
                    mEtLoginInputName.setFocusable(true);
                    return;
                }
                PrefUtils.setString(LoginActivity.this, "student", result);
                //获取声纹文本
                getPwd();
            }
        });

    }

    private void bind() {
        final String[] s = new String[]{"绑定学生mac地址", "绑定教师mac地址", "绑定学生声纹", "测试音频API", "测试wifiAPI"};
        new AlertView("请选择绑定类型", null, "取消", null, s, this, AlertView.Style.ActionSheet, new OnItemClickListener() {

            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    bindStudentMacAddress();
                } else if (position == 1) {
                    bindTeacherMacAddress();
                } else if (position == 2) {
                    bindVoice();
                } else if (position == 3) {
                    testVoice();
                } else if (position == 4) {
                    testWifi();
                }
            }
        }).setCancelable(true).show();
    }

    /**
     * 忘记密码
     */
    private void fogotPwd() {
        UIUtil.okTips(this, "亲稍等哦", "火速开发中...");
    }

    private void login() {
        String name = mEtLoginInputName.getText().toString().trim();
        String pwd = mEtLoginInputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            UIUtil.showToast(getApplicationContext(), "请输入用户名或者密码");
            return;
        }
        int checkedRadioButtonId = mRgUsertype.getCheckedRadioButtonId();
        final Gson gson = GsonBuilderUtil.create();
        if (checkedRadioButtonId == R.id.rb_student) {

            RequestParams params = new RequestParams(Constant.URL_LOGIN_STUDENT);
            params.addQueryStringParameter("studentId", name);
            params.addQueryStringParameter("pwd", pwd);

            x.http().post(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.i("TAG", "登录获取学生信息:" + result);
                    if (TextUtils.isEmpty(result)) {
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Student student = gson.fromJson(result, Student.class);
                    if (student != null && student.getId() != null) {
                        PrefUtils.setString(getApplication(), "student", result);
                        if (LOGIN_TYPE == BIND_MAC_ADDRESS) {
                            Intent it = new Intent(LoginActivity.this, StudentInfoActivity.class);
                            startActivity(it);
                            return;
                        }
                        if (LOGIN_TYPE == LOGIN) {
                            Intent it = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(it);
                            finish();
                            return;
                        }
                        if (LOGIN_TYPE == BIND_VOICE) {
                            finish();
                            Intent it = new Intent(LoginActivity.this, RegisterVoiceActivity.class);
                            startActivity(it);
                        }
                    } else {
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
        } else {
            RequestParams params = new RequestParams(Constant.URL_LOGIN_TEACHER);
            params.addQueryStringParameter("teacherId", name);
            params.addQueryStringParameter("pwd", pwd);

            x.http().post(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.i("TAG", "登录获取教师信息:" + result);
                    if (TextUtils.isEmpty(result)) {
                        Toast.makeText(x.app(), "用户名或者密码错误", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> map = gson.fromJson(result, new TypeToken<Map<String, Object>>() {
                    }.getType());

                    if (map != null && map.get("id") != null) {
                        PrefUtils.setString(getApplication(), "teacher", result);
                        if (LOGIN_TYPE == BIND_MAC_ADDRESS) {
                            Intent it = new Intent(LoginActivity.this, TeacherSettingActivity.class);
                            startActivity(it);
                            return;
                        }
                        if (LOGIN_TYPE == LOGIN) {
                            Intent it = new Intent(LoginActivity.this, TeacherHomeActivity.class);
                            startActivity(it);
                            finish();
                            return;
                        }
                    } else {
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

    private void bindTeacherMacAddress() {
        LOGIN_TYPE = BIND_MAC_ADDRESS;
        mRgUsertype.check(R.id.rb_teacher);
        login();
    }

    private void bindStudentMacAddress() {
        LOGIN_TYPE = BIND_MAC_ADDRESS;
        mRgUsertype.check(R.id.rb_student);
        login();

    }

    private void testWifi() {
        Intent it = new Intent(this, TestWifiActivity.class);
        startActivity(it);
    }

    private void testVoice() {
        Intent it = new Intent(this, VoiceRegisterActivity.class);
        startActivity(it);
    }

    private void bindVoice() {
        if (mRgUsertype.getCheckedRadioButtonId() == R.id.rb_teacher) {
            showTip("目前声纹录入只支持学生端");
            return;
        }
        String name = mEtLoginInputName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showTip("请输入学号");
            return;
        }
        mAuId = "s" + name;
        Map<String, String> params = new HashMap<>();
        params.put("studentId", name);
        NetWorkUtil.postYN(Constant.URL_LOGIN_STUDENT_CHECK, params, new NetWorkUtil.WorkerYN() {
            @Override
            public void success(String result, Gson gson) {
                if (TextUtils.isEmpty(result) || result.length() < 3) {
                    showTip("用户名错误,请重新输入");
                    mEtLoginInputName.setText("");
                    mEtLoginInputName.setFocusable(true);
                    return;
                }
                PrefUtils.setString(LoginActivity.this, "student", result);
                LOGIN_TYPE = BIND_VOICE;
                login();
            }
        });
    }


    /**
     * 获取声纹密码
     */
    private void getPwd() {
        mVerifier.cancel();
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        mVerifier.getPasswordList(mPwdListenter);
    }

    /**
     * 获取密码
     */
    private SpeechListener mPwdListenter = new SpeechListener() {
        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

            String result = new String(buffer);
            try {
                JSONObject object = new JSONObject(result);
                if (!object.has("txt_pwd")) {
                    return;
                }
                JSONArray pwdArray = object.optJSONArray("txt_pwd");
                mTextPwd = pwdArray.getString(0);
                UIUtil.ok(LoginActivity.this, "是否开始录音", "欢迎使用声纹登录", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        valid();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                showTip("获取声纹文本失败,请检查网络");
            }
        }
    };

    /**
     * 验证声纹是否通过
     */
    private void valid() {
        // 清空提示信息
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
        mVerifier = SpeakerVerifier.getVerifier();
        // 设置业务类型为验证
        mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
        //			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
        // 文本密码注册需要传入密码
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        UIUtil.okNoCancel(LoginActivity.this, "请确认", "开始验证声纹\n确认后请读出:" + mTextPwd, new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                // 开始验证
                mVerifier.startListening(mVerifyListener);
            }
        });


    }

    /**
     * 验证模型
     */
    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d("TAG", "返回音频数据：" + data.length);
        }

        @Override
        public void onResult(VerifierResult result) {
            if (result.ret == 0) {
                // 验证通过
                UIUtil.ok(LoginActivity.this, "是否进入主页", "验证通过", new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        Intent it = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(it);
                        finish();
                    }
                });
            } else {
                String msg = "";
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        msg = "内核异常";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        msg = "出现截幅";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        msg = "太多噪音";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        msg = "录音太短";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        msg = "验证不通过，您所读的文本不一致";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        msg = "音量太低";
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        msg = "音频长达不到自由说的要求";
                        break;
                    default:
                        break;
                }
                UIUtil.alert(LoginActivity.this, "验证失败", "失败原因:\n" + msg, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                    }
                });
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            switch (error.getErrorCode()) {
                case ErrorCode.MSP_ERROR_NOT_FOUND:
                    UIUtil.alert(LoginActivity.this, "你尚未绑定声纹", "请先绑定声纹", new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                        }
                    });
                    break;
                default:
                    showTip("onError Code：" + error.getPlainDescription(true));
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


}
