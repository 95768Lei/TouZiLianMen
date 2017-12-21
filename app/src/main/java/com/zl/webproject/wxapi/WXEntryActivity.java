package com.zl.webproject.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zl.webproject.R;
import com.zl.webproject.activity.MainActivity;
import com.zl.webproject.utils.LoginEvent;
import com.zl.webproject.utils.ShareEvent;
import com.zl.webproject.utils.weixinpay.Constants;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by HUANG on 2016/7/25 0025.
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {


    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:

                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:

                break;
            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {

        String result = null;
        String str = "";
        if (Constants.wxtype == Constants.WXSHARE) {
            str = "分享";
        } else {
            str = "登录";
        }
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK: {

                if (Constants.wxtype == Constants.WXLOGIN) {
                    String code = ((SendAuth.Resp) resp).code;
                    LoginEvent loginEvent = new LoginEvent();
                    loginEvent.setOk(true);
                    loginEvent.setCode(code);
                    EventBus.getDefault().post(loginEvent);
                } else {
                    ShareEvent shareEvent = new ShareEvent();
                    shareEvent.setOk(true);
                    EventBus.getDefault().post(shareEvent);
                }
                result = str + "成功";
            }
            break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = str + "取消";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = str + "被拒绝";
                break;
            default:
                result = str + "返回" + resp.errCode;
                break;
        }

        if (resp.errCode != BaseResp.ErrCode.ERR_OK) {
            //微信分享失败
            if (Constants.wxtype != Constants.WXLOGIN) {
                ShareEvent shareEvent = new ShareEvent();
                shareEvent.setOk(false);
                EventBus.getDefault().post(shareEvent);
            } else {
                //微信登录失败
                LoginEvent loginEvent = new LoginEvent();
                loginEvent.setOk(false);
                EventBus.getDefault().post(loginEvent);
            }
        }

//        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        finish();
    }
}
