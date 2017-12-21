package com.zl.webproject.dialog;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zl.webproject.R;
import com.zl.webproject.activity.MainActivity;
import com.zl.webproject.utils.API;
import com.zl.webproject.utils.Util;
import com.zl.webproject.utils.weixinpay.Constants;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tencent.connect.share.QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;

/**
 * Created by zhanglei on 2017/7/14.
 * 分享的弹窗
 */

public class ShareDialog implements View.OnClickListener {
    private Activity context;
    private PopupWindow window;
    private View parentView;
    private String url, title, content;
    private int scene = SendMessageToWX.Req.WXSceneSession;
    private Tencent mTencent;
    private View shareQQ;
    private View shareQZone;
    private View shareWxFriend;
    private View shareWxQuan;
    private String imageUrl;
    private IUiListener iUiListener;

    public void setiUiListener(IUiListener iUiListener) {
        this.iUiListener = iUiListener;
    }

    public ShareDialog(Activity context, View parentView) {
        this.context = context;
        this.parentView = parentView;
        initView();
        initListener();
    }

    private void initView() {
        mTencent = Tencent.createInstance(Constants.TencentId, context.getApplicationContext());

        View view = LayoutInflater.from(context).inflate(R.layout.share_layout, null);
        shareQQ = view.findViewById(R.id.share_qq);
        shareQZone = view.findViewById(R.id.share_qzone);
        shareWxFriend = view.findViewById(R.id.share_wx_friends);
        shareWxQuan = view.findViewById(R.id.share_wx_quan);
        window = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable(true);
        window.setTouchable(true);
        window.setBackgroundDrawable(new BitmapDrawable());
        window.setAnimationStyle(R.style.choose_pop_window_anim_style);
    }

    private void initListener() {
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setAlpha(1f);
            }
        });

        shareQQ.setOnClickListener(this);
        shareQZone.setOnClickListener(this);
        shareWxFriend.setOnClickListener(this);
        shareWxQuan.setOnClickListener(this);
    }

    /**
     * 显示分享界面
     */
    public void showShare(String url, String title, String content, String imageUrl) {
        this.url = url;
        this.imageUrl = imageUrl;
        this.title = title;
        this.content = content;
        window.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        setAlpha(0.6f);
    }

    public Tencent getTencent() {
        return mTencent;
    }

    private void shareWx() {

        new Thread() {
            @Override
            public void run() {
                final IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, true);
                //将应用的appId注册到微信
                api.registerApp(Constants.APP_ID);
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = url;

                final WXMediaMessage message = new WXMediaMessage(webpage);
                message.title = title;
                message.description = content;
                message.thumbData = Util.getHtmlByteArray(imageUrl);

                //构造一个Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = String.valueOf(System.currentTimeMillis());
                req.message = message;
                req.scene = scene;
                api.sendReq(req);
            }
        }.start();
    }

    private void shareQZone() {
        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, content);//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);//必填
//        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, MainActivity.shareImagePath);
        ArrayList<String> imglist = new ArrayList<String>();
        imglist.add(imageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imglist);
        if (mTencent != null) {
            mTencent.shareToQzone(context, params, iUiListener);
        } else {
            Toast.makeText(context, "分享初始化中，请稍候", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQQ() {

        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//必填
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);//选填
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);//必填
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
//        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, MainActivity.shareImagePath);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, context.getResources().getString(R.string.app_name));
        if (mTencent != null) {
            mTencent.shareToQQ(context, params, iUiListener);
        } else {
            Toast.makeText(context, "分享初始化中，请稍候", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 消除Dialog
     */
    public void dismiss() {
        window.dismiss();
    }

    /**
     * 设置界面的透明度
     */
    private void setAlpha(float f) {
        Window window = context.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = f;
        window.setAttributes(attributes);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //分享到好友列表
            case R.id.share_wx_friends:
                scene = SendMessageToWX.Req.WXSceneSession;
                shareWx();
                break;
            //分享到朋友圈
            case R.id.share_wx_quan:
                scene = SendMessageToWX.Req.WXSceneTimeline;
                shareWx();
                break;
            //分享到QQ好友
            case R.id.share_qq:
                scene = SendMessageToWX.Req.WXSceneTimeline;
                shareQQ();
                break;
            //分享到QQ空间
            case R.id.share_qzone:
                scene = SendMessageToWX.Req.WXSceneTimeline;
                shareQZone();
                break;
        }
        dismiss();
    }

}
