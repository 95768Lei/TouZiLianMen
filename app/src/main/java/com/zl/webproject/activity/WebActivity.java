package com.zl.webproject.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zl.webproject.R;
import com.zl.webproject.dialog.ShareDialog;
import com.zl.webproject.utils.ShareEvent;
import com.zl.webproject.utils.weixinpay.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class WebActivity extends AppCompatActivity {

    private AppCompatActivity mActivity;
    private WebView webView;
    private String url;
    private ShareDialog shareDialog;
    private MyIUiListener listener = new MyIUiListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        initView();
    }

    private void initView() {
        url = getIntent().getStringExtra("url");
        webView = (WebView) findViewById(R.id.main_web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        //设置在本应用内打开网页
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.addJavascriptInterface(new JavaInterface(), "android");

        shareDialog = new ShareDialog(mActivity, webView);
        shareDialog.setiUiListener(listener);

        //注册EventBus
        EventBus.getDefault().register(this);
    }

    /**
     * 接受EventBus 发送的分享的结果
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(ShareEvent event) {
        //微信分享是否成功，会回调这个方法
        webView.loadUrl("javascript:hideLoad()");
        if (event.isOk()) {
            //微信分享成功，同时通知H5页面
            webView.loadUrl("javascript:forwardSaveWeChat(" + true + ")");
        } else {
            //微信分享失败，同时通知H5页面
            webView.loadUrl("javascript:forwardSaveWeChat(" + false + ")");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, listener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null) {
                webView.loadUrl("javascript:goBackReferrer()");
            }
        }
        return true;
    }

    private class JavaInterface {

        /**
         * 吊起分享功能
         */
        @JavascriptInterface
        public void share(final String url, final String title, final String content, final String imageUrl) {
            //将微信的类型变成 登录
            Constants.wxtype = Constants.WXSHARE;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shareDialog.showShare(url, title, content, imageUrl);
                }
            });
        }

        /**
         * 拨打电话
         *
         * @param number
         */
        @JavascriptInterface
        public void callPhone(String number) {
            //用intent启动拨打电话
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            startActivity(intent);
        }

        /**
         * 打开新界面
         */
        @JavascriptInterface
        public void openNewPage(String url) {
            Intent intent = new Intent(mActivity, WebActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }

        /**
         * 关闭界面
         */
        @JavascriptInterface
        public void finish() {
            mActivity.finish();
        }
    }

    /**
     * QQ分享的回调接口
     */
    public class MyIUiListener implements IUiListener {
        @Override
        public void onComplete(Object o) {
            webView.loadUrl("javascript:hideLoad()");
            //QQ分享成功，同时通知H5页面
            webView.loadUrl("javascript:forwardSave(" + true + ")");
        }

        @Override
        public void onError(UiError uiError) {
            webView.loadUrl("javascript:hideLoad()");
            webView.loadUrl("javascript:forwardSave(" + false + ")");
        }

        @Override
        public void onCancel() {
            webView.loadUrl("javascript:hideLoad()");
            webView.loadUrl("javascript:forwardSave(" + false + ")");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            webView.destroy();
            webView = null;
        } catch (Exception e) {

        }
    }
}
