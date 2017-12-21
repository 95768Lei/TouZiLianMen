package com.zl.webproject.utils.weixinpay;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

/**
 * Created by zhanglei on 2017/7/19.
 */

public class WeiXinPayUtils {

    private Activity mActivity;
    private final IWXAPI msgApi;
    private StringBuffer sb;
    public static int wxPayType = -1;
    public static int BS = 1; //购买包时劵
    public static int CZ = 2; //微信充值
    public static int SJ = 3;  //微信上机
    public static int BM = 4;  //微信报名

    /**
     * @param mActivity 上下文
     */
    public WeiXinPayUtils(Activity mActivity, int type) {
        this.mActivity = mActivity;
        msgApi = WXAPIFactory.createWXAPI(mActivity, Constants.APP_ID);
        msgApi.registerApp(Constants.APP_ID);
        sb = new StringBuffer();
        WeiXinPayUtils.wxPayType = type;
    }

    protected void showError(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取请求使用的RequestBody
     *
     * @param params
     * @return
     */
//    protected RequestBody getRequestBody(Map<String, String> params) {
//        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new JSONObject(params).toString());
//    }

    /**
     * @param amount
     */
    public void weiXinPay(float amount) {
        if (!msgApi.isWXAppInstalled()) {
            //提醒用户没有按照微信
            showError("请安装微信");
            return;
        } else {
            chatPay(amount);
        }
    }

    /**
     * 获取微信充值所需参数
     */
    private void chatPay(float amount) {


    }

    /**
     * 吊起微信支付
     */
    private void weiXinPay(String payData) {
//        WeiXinPayBean data = new Gson().fromJson(payData, WeiXinPayBean.class);
//        IWXAPI msgApi = WXAPIFactory.createWXAPI(mActivity, data.getAppid());
//        // 将该app注册到微信
//        msgApi.registerApp(data.getAppid());
//
//        PayReq request = new PayReq();
//        request.appId = data.getAppid();
//        request.partnerId = data.getPartnerid();
//        request.prepayId = data.getPrepayid();
//        request.packageValue = data.getPackageX();
//        request.nonceStr = data.getNoncestr();
//        request.timeStamp = String.valueOf(data.getTimestamp());
//        request.sign = data.getSign();
//        msgApi.sendReq(request);
    }

//    /**
//     * 调用微信支付
//     *
//     * @param payOrderBean
//     */
//    private void weiXinPay(PayOrderBean payOrderBean) {
//        PayReq request = new PayReq();
//        request.appId = Constants.APP_ID;
//        request.partnerId = Constants.MCH_ID;
//        request.prepayId = payOrderBean.getPrepay_id();
//        request.packageValue = "Sign=WXPay";
//        request.nonceStr = getNonceStr();
//        request.timeStamp = String.valueOf(getTimeStamp());
//        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
//        signParams.add(new BasicNameValuePair("appid", request.appId));
//        signParams.add(new BasicNameValuePair("noncestr", request.nonceStr));
//        signParams.add(new BasicNameValuePair("package", request.packageValue));
//        signParams.add(new BasicNameValuePair("partnerid", request.partnerId));
//        signParams.add(new BasicNameValuePair("prepayid", request.prepayId));
//        signParams.add(new BasicNameValuePair("timestamp", request.timeStamp));
//        request.sign = getAppSign(signParams);
//        msgApi.sendReq(request);
//    }
//
//    /**
//     * 获取十位的时间戳
//     *
//     * @returnt
//     */
//    private long getTimeStamp() {
//        return System.currentTimeMillis() / 1000;
//    }
//
//
//    /**
//     * 生成签名
//     */
//    private String getAppSign(List<NameValuePair> params) {
//
//        StringBuilder sb = new StringBuilder();
//
//        for (int i = 0; i < params.size(); i++) {
//            sb.append(params.get(i).getName());
//            sb.append('=');
//            sb.append(params.get(i).getValue());
//            sb.append('&');
//        }
//        sb.append("key=");
//        sb.append(Constants.API_KEY);
//        this.sb.append("sign str\n" + sb.toString() + "\n\n");
//        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
//        Log.e("orion", appSign);
//        return appSign;
//    }
//
//    /**
//     * 生成随机数
//     *
//     * @return
//     */
//    private String getNonceStr() {
//        Random random = new Random();
//        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
//                .getBytes());
//    }
}
