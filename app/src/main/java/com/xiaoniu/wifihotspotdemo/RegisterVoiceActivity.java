package com.xiaoniu.wifihotspotdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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

import java.util.HashMap;
import java.util.Map;

import static com.xiaoniu.wifihotspotdemo.R.id.rl_check;
import static com.xiaoniu.wifihotspotdemo.R.id.rl_del;
import static com.xiaoniu.wifihotspotdemo.R.id.rl_que;
import static com.xiaoniu.wifihotspotdemo.R.id.rl_register;
import static com.xiaoniu.wifihotspotdemo.R.id.tv_back;

/**
 * Created by think on 2017/5/13 18:29.
 * 注册声纹界面
 */

public class RegisterVoiceActivity extends BaseActivity  implements View.OnClickListener{

    private TextView mTvBack;
    private RelativeLayout mRlVoicePwd;
    private EditText mTvVoicePwd;
    private RelativeLayout mRlRegister;
    private EditText mTvRegister;
    private TextView mTvTip1;
    private TextView mTvTip2;
    private TextView mTvTip3;
    private RelativeLayout mRlCheck;
    private EditText mTvCheck;
    private RelativeLayout mRlQue;
    private RelativeLayout mRlDel;

    //用户唯一标示
    private String mAuId;

    //文本声纹密码类型
    private static final int PWD_TYPE_TEXT = 1;
    //文本声纹密码
    private String mTextPwd;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;
    private Toast mToast;
    private Student mStudent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_voice);
        inintView();
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

    private void inintView() {
        String json = PrefUtils.getString(this, "student",null);
        if(TextUtils.isEmpty(json)) {
            UIUtil.showToast(this, "登录失效,请重新登录");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Gson gson = GsonBuilderUtil.create();
        mStudent = gson.fromJson(json, Student.class);
        String voice = mStudent.getVoice();
        mAuId = "s"+mStudent.getStuId();

        mTvBack = (TextView) findViewById(R.id.tv_back);
        mRlVoicePwd = (RelativeLayout) findViewById(R.id.rl_voice_pwd);
        mTvVoicePwd = (EditText) findViewById(R.id.tv_voice_pwd);
        mRlRegister = (RelativeLayout) findViewById(rl_register);
        mTvRegister = (EditText) findViewById(R.id.tv_register);
        mTvTip1 = (TextView) findViewById(R.id.tv_tip1);
        mTvTip2 = (TextView) findViewById(R.id.tv_tip2);
        mTvTip3 = (TextView) findViewById(R.id.tv_tip3);
        mRlQue = (RelativeLayout) findViewById(R.id.rl_que);
        mRlDel = (RelativeLayout) findViewById(R.id.rl_del);
        mRlCheck = (RelativeLayout) findViewById(R.id.rl_check);
        mTvCheck = (EditText) findViewById(R.id.tv_check);



        if(!TextUtils.isEmpty(voice) && voice.matches("\\w+#\\w+")){
            String[] split = voice.split("#");
            mTvVoicePwd.setText(split[0]);
            mTvRegister.setText("注册成功");
        }

        mTvBack.setOnClickListener(this);
        mRlVoicePwd.setOnClickListener(this);
        mRlRegister.setOnClickListener(this);
        mRlCheck.setOnClickListener(this);
        mRlQue.setOnClickListener(this);
        mRlDel.setOnClickListener(this);


        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case tv_back:
                back();
                break;
            case R.id.rl_voice_pwd:
                getPwd();
                break;
            case rl_register:
                register();
                break;
            case rl_check:
                valid();
                break;
            case rl_que:
                que();
                break;
            case rl_del:
                del();
                break;
            default:
                break;
        }
    }




    /**
     * 验证声纹是否通过
     */
    private void valid() {
        if(TextUtils.isEmpty(mTextPwd)){
            UIUtil.alert(this, "无法验证","请先获取声纹密码", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    getPwd();
                }
            });
            return;
        }

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
        if (TextUtils.isEmpty(mTextPwd)) {
            showTip("请获取密码后进行操作");
            return;
        }
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        mTvTip1.setText("请读出："+ mTextPwd);
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        // 开始验证
        mVerifier.startListening(mVerifyListener);

    }



    /**
     * 获取声纹密码
     */
    private void getPwd() {
        mVerifier.cancel();
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant. ISV_PWDT , "" + PWD_TYPE_TEXT);
        mVerifier.getPasswordList(mPwdListenter);
    }

    /**
     * 删除模型
     */
    public void del() {
        UIUtil.alert(this, "请确认","是否删除声纹", new UIUtil.AlterCallBack() {
            @Override
            public void confirm() {
                performModelOperation("del", mModelOperationListener);
            }
        });

    }

    /**
     * 查询模型
     */
    public void que() {
        performModelOperation("que", mModelOperationListener);
    }



    /**
     * 注册声纹
     */
    private void register() {
        if(TextUtils.isEmpty(mTextPwd)){
            UIUtil.alert(this, "无法注册","请先获取声纹密码", new UIUtil.AlterCallBack() {
                @Override
                public void confirm() {
                    getPwd();
                }
            });
            return;
        }

        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        mTvTip1.setText("请读出: " + mTextPwd);
        mTvTip2.setText("训练 第" + 1 + "遍,剩余4遍");

        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuId);
        // 设置业务类型为注册
        mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
        // 设置声纹密码类型
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        // 开始注册
        mVerifier.startListening(mRegisterListener);
    }

    /**
     * 验证模型
     */
    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d("TAG", "返回音频数据："+data.length);
        }

        @Override
        public void onResult(VerifierResult result) {

            if (result.ret == 0) {
                // 验证通过
                mTvCheck.setText("验证通过");
                mTvTip2.setText("验证通过");
            }
            else{
                mTvCheck.setText("验证不通过 ");
                mTvTip2.setText("验证不通过");
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mTvTip3.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mTvTip3.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mTvTip3.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mTvTip3.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mTvTip3.setText("验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mTvTip3.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mTvTip3.setText("音频长达不到自由说的要求");
                        break;
                    default:
                        mTvTip3.setText("");
                        break;
                }
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
                    mTvTip2.setText("模型不存在，请先注册");
                    break;

                default:
                    showTip("onError Code："	+ error.getPlainDescription(true));
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


    /**
     * 训练注册语音
     */
    private VerifierListener mRegisterListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onResult(final VerifierResult result) {

            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mTvTip3.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        mTvTip3.setText("训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mTvTip3.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mTvTip3.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mTvTip3.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mTvTip3.setText("训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mTvTip3.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mTvTip3.setText("音频长达不到自由说的要求");
                        break;
                    default:
                        mTvTip3.setText("");
                        break;
                }

                if (result.suc == result.rgn) {
                    mTvRegister.setText("注册成功");
                    mTvTip2.setText("训练结束");
                    mTvTip3.setText("");
                    UIUtil.ok(RegisterVoiceActivity.this, "是否上传声纹信息","注册成功\n您的文本密码声纹ID:\n"+ result.vid, new UIUtil.AlterCallBack() {
                        @Override
                        public void confirm() {
                            uploadVoice(result.vid);
                        }
                    });
                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;
                    mTvTip1.setText("请读出: " + mTextPwd);
                    mTvTip2.setText("训练 第" + nowTimes + "遍,剩余" + leftTimes + "遍");
                }

            }else {
                mTvRegister.setText("注册失败 , 点击重新开始。");
                mTvVoicePwd.setText("点击获取");
                mTextPwd = "";
                mTvTip1.setText("");
                mTvTip2.setText("");
                mTvTip3.setText("");
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
            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
                showTip("模型已存在，如需重新注册，请先删除");
                mTvTip1.setText("");
                mTvTip2.setText("");
            } else {
                showTip("onError Code：" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }
    };

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
                mTvVoicePwd.setText(mTextPwd);
                UIUtil.ok(RegisterVoiceActivity.this, "是否注册声纹", "获取声纹密码成功\n声纹密码:" + mTextPwd, new UIUtil.AlterCallBack() {
                    @Override
                    public void confirm() {
                        register();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                mTvVoicePwd.setText("获取密码失败,点击重试");
            }
        }
    };

    /**
     * 模型操作
     */
    private SpeechListener mModelOperationListener = new SpeechListener() {

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

            String result = new String(buffer);
            try {
                JSONObject object = new JSONObject(result);
                String cmd = object.getString("cmd");
                int ret = object.getInt("ret");

                if ("del".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        showTip("删除成功");
                        mTextPwd = "";
                        mTvRegister.setText("");
                        mTvVoicePwd.setText("");
                        mTvTip1.setText("");
                        mTvTip2.setText("");
                        mTvTip3.setText("");
                        mTvCheck.setText("");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        showTip("删除失败，模型不存在");
                    }
                } else if ("que".equals(cmd)) {
                    if (ret == ErrorCode.SUCCESS) {
                        showTip("模型存在");
                    } else if (ret == ErrorCode.MSP_ERROR_FAIL) {
                        showTip("模型不存在");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                showTip("操作失败：" + error.getPlainDescription(true));
            }
        }
    };

    /**
     * 执行模型操作
     *
     * @param operation 操作命令
     * @param listener  操作结果回调对象
     */
    private void performModelOperation(String operation, SpeechListener listener) {
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        // 文本密码删除需要传入密码
        if (TextUtils.isEmpty(mTextPwd)) {
            showTip("请获取密码后进行操作");
            return;
        }
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        // 设置auth_id，不能设置为空
        mVerifier.sendRequest(operation, mAuId, listener);
    }

    /**
     * 上传voice
     */
    private void uploadVoice(String voice) {
        Map<String,String> params = new HashMap<>();
        params.put("studentId",mStudent.getStuId()+"");
        params.put("voice",voice);
        NetWorkUtil.post(Constant.URL_STUDNET_BIND_VOICE, params, new NetWorkUtil.Worker() {
            @Override
            public void success(String result, Gson gson) {
                showTip("上传声纹成功");
            }
        });
    }

    /**
     * 退出当前界面
     */
    private void back() {
        finish();
        Intent it = new Intent(this,LoginActivity.class);
        startActivity(it);
    }

    /**
     * 重写返回键方法
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    protected void onDestroy() {
//        if (null != mVerifier) {
//            mVerifier.stopListening();
//            mVerifier.destroy();
//        }
//        super.onDestroy();
//    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

}
