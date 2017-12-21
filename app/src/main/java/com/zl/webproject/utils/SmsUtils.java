package com.zl.webproject.utils;

import com.tencent.mm.opensdk.utils.Log;

import cn.jpush.sms.SMSSDK;
import cn.jpush.sms.listener.SmscheckListener;
import cn.jpush.sms.listener.SmscodeListener;

/**
 * Created by 张磊 on 2017/8/17.
 * 短信工具类
 */

public class SmsUtils {

    /**
     * 发送短信
     *
     * @param phone
     */
    public static void sendSms(String phone) {
        SMSSDK.getInstance().getSmsCode(phone, "1", new SmscodeListener() {
            @Override
            public void getCodeSuccess(final String uuid) {
                // 获取验证码成功，uuid 为此次获取的唯一标识码。
                Log.e("uuid", uuid);
            }

            @Override
            public void getCodeFail(int errCode, final String errMsg) {
                // 获取验证码失败 errCode 为错误码，详情请见文档后面的错误码表；errMsg 为错误描述。
                Log.e("uuid", "errCode:" + errCode + "\nerrMsg" + errMsg);
            }
        });
    }

    /**
     * 发送短信
     *
     * @param phone
     */
    public static void sendSms(String phone, SmscodeListener listener) {
        SMSSDK.getInstance().getSmsCode(phone, "1", listener);
    }

    /**
     * 进行短信验证
     *
     * @param phone
     * @param code
     */
    public static void checkSmsCode(String phone, String code, SmscheckListener listener) {
        SMSSDK.getInstance().checkSmsCodeAsyn(phone, code, listener);
    }

}
