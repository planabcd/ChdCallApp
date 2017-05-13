package com.xiaoniu.wifihotspotdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.xiaoniu.wifihotspotdemo.common.BaseActivity;
import com.xiaoniu.wifihotspotdemo.util.UIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by think on 2017/5/13 10:37.
 */

public class VoiceRegisterActivity extends BaseActivity{
    private EditText mEtAuid;
    private TextView mTvTip1;
    private TextView mTvTip2;
    private Toast mToast;
    private TextView mTvTip3;
    private TextView mTvTip4;
    private TextView mTvTip5;


    private static final int PWD_TYPE_TEXT = 1;
    private String mTextPwd;

    private String mAuid;
    // 声纹识别对象
    private SpeakerVerifier mVerifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_register);
        initView();

        // 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
        mVerifier = SpeakerVerifier.createVerifier(this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    UIUtil.showToastS(VoiceRegisterActivity.this,"引擎初始化成功");
                    mTvTip5.setText("引擎初始化成功");
                } else {
                    mTvTip5.setText("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
    }

    private void initView() {
        mEtAuid = (EditText) findViewById(R.id.et_auid);
        mTvTip1 = (TextView) findViewById(R.id.tv_tip1);
        mTvTip2 = (TextView) findViewById(R.id.tv_tip2);
        mTvTip3 = (TextView) findViewById(R.id.tv_tip3);
        mTvTip4 = (TextView) findViewById(R.id.tv_tip4);
        mTvTip5 = (TextView) findViewById(R.id.tv_tip5);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
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

        String s = mEtAuid.getText().toString();
        // 设置auth_id，不能设置为空
        mVerifier.sendRequest(operation, s, listener);
    }


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
     * 删除模型
     * @param v
     */
    public void del(View v) {
        performModelOperation("del", mModelOperationListener);
    }

    /**
     * 查询模型
     * @param v
     */
    public void que(View v) {
        performModelOperation("que", mModelOperationListener);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("TAG", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * ui1
     * @param v
     */
    public void ui1(View v) {

        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, mInitListener);
        //2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        //4.显示dialog，接收语音输入
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //TODO
            }
        });
        mDialog.show();
    }




    /**
     * 验证模型
     * @param v
     */
    public void yn(View v) {
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
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mEtAuid.getText().toString());
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        // 开始验证
        mVerifier.startListening(mVerifyListener);
    }

    private VerifierListener mVerifyListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d("TAG", "返回音频数据："+data.length);
        }

        @Override
        public void onResult(VerifierResult result) {
            mTvTip1.setText(result.source);

            if (result.ret == 0) {
                // 验证通过
                mTvTip2.setText("验证通过");
            }
            else{
                // 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mTvTip2.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mTvTip2.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mTvTip2.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mTvTip2.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mTvTip2.setText("验证不通过，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mTvTip2.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mTvTip2.setText("音频长达不到自由说的要求");
                        break;
                    default:
                        mTvTip2.setText("验证不通过");
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
     * 获取密码
     * @param v
     */
    public void getPwd(View v){
        mVerifier.cancel();
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant. ISV_PWDT , "" + PWD_TYPE_TEXT);
        mVerifier.getPasswordList(mPwdListenter);
    }
    /**
     * 开始注册
     * @param v
     */
    public void register(View v){
        if(TextUtils.isEmpty(mTextPwd)){
            UIUtil.showToastS(this,"先获取密码");
            return;
        }

        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
        // 设置auth_id，不能设置为空
        String mAuthId = mEtAuid.getText().toString();
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
        mTvTip1.setText("请读出：" + mTextPwd);
        mTvTip2.setText("训练 第" + 1 + "遍，剩余4遍");


        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        // 设置业务类型为注册
        mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
        // 设置声纹密码类型
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_TEXT);
        // 开始注册
        mVerifier.startListening(mRegisterListener);
    }

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
                        mTvTip4.setText("获取密码成功:"+mTextPwd);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                mTvTip4.setText("获取密码失败:"+error.getErrorCode());
            }
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
        public void onResult(VerifierResult result) {

            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        mTvTip1.setText("内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        mTvTip1.setText("训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        mTvTip1.setText("出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        mTvTip1.setText("太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        mTvTip1.setText("录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        mTvTip1.setText("训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        mTvTip1.setText("音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        mTvTip1.setText("音频长达不到自由说的要求");
                    default:
                        mTvTip1.setText("");
                        break;
                }

                if (result.suc == result.rgn) {
                    mTvTip2.setText("注册成功");
                    mTvTip3.setText("您的文本密码声纹ID：\n" + result.vid);

                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;
                    mTvTip1.setText("请读出：" + mTextPwd);
                    mTvTip2.setText("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                }

            }else {
                mTvTip1.setText("注册失败，请重新开始。");
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


    @Override
    protected void onDestroy() {
        if (null != mVerifier) {
            mVerifier.stopListening();
            mVerifier.destroy();
        }
        super.onDestroy();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

}
