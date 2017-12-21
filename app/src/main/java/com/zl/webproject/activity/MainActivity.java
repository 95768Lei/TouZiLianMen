package com.zl.webproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.foamtrace.photopicker.PhotoPickerActivity;
import com.foamtrace.photopicker.SelectModel;
import com.foamtrace.photopicker.intent.PhotoPickerIntent;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.utils.Log;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zl.webproject.R;
import com.zl.webproject.dialog.ShareDialog;
import com.zl.webproject.utils.API;
import com.zl.webproject.utils.HttpUtils;
import com.zl.webproject.utils.LocationUtils;
import com.zl.webproject.utils.LoginEvent;
import com.zl.webproject.utils.NetUtils;
import com.zl.webproject.utils.PhotoBitmapUtils;
import com.zl.webproject.utils.SmsUtils;
import com.zl.webproject.utils.SpUtlis;
import com.zl.webproject.utils.UpdateManager;
import com.zl.webproject.utils.alipay.AlipayUtils;
import com.zl.webproject.utils.weixinpay.Constants;
import com.zl.webproject.utils.weixinpay.WeiXinPayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.sms.listener.SmscheckListener;
import cn.jpush.sms.listener.SmscodeListener;
import okhttp3.Request;

/**
 * @author zhanglei
 * @date 17/08/06
 * 应用首页（Web）
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CAMERA_CODE = 0;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int REQUEST_CAMERA_SIGN_CODE = 2;
    public static final int USER_REQUEST_CAMERA_SIGN_CODE = 3;
    public static final int USER_PICK_FROM_CAMERA = 4;
    public static final int PERSON_IDCARD_CAMERA = 5;
    public static final int PERSON_IDCARD_PICK = 6;
    //是不是QQ分享
    private boolean isShare = true;
    private WebView webView;
    private String path;
    private LocationUtils locationLogin;
    private LocationUtils locationGetData;
    private AlipayUtils alipayUtils;
    private WeiXinPayUtils weiXinPayUtils;
    private ArrayList<String> photoList = new ArrayList<>();
    private IWXAPI wxapi;
    private MyIUiListener listener;
    private Tencent mTencent;
    private Activity mActivity;
    public static final String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tzlm";
    public static final String shareImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tzlm/tzlm.png";
    private ShareDialog shareDialog;
    private View linear;
    private TextView main_text;
    private static Handler handler = new Handler();
    //是否是第一次定位
    private boolean isOne = true;
    private UpdateManager updateManager;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

//        API.init(this);

        initView();
        init();
        initListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl("javascript:hideLoad()");
    }

    private void initView() {

        url = getIntent().getStringExtra("url");

        webView = (WebView) findViewById(R.id.main_web);
        linear = findViewById(R.id.main_linear);
        main_text = (TextView) findViewById(R.id.main_text);
        webView.getSettings().setJavaScriptEnabled(true);

        //设置在本应用内打开网页
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        if (TextUtils.isEmpty(url)) {
            webView.loadUrl(API.BASEURL);
        } else {
            webView.loadUrl(url);
        }
        webView.addJavascriptInterface(new JavaInterface(), "android");

        main_text.setText(Html.fromHtml("网络连接中断,请点击<font color=\"#0BB4B0\">重试</font> "));

        //注册EventBus
        EventBus.getDefault().register(this);
    }

    /**
     * 登录
     */
    private void login(final String data, final String code) {

        //获取用户账号密码
        String[] loginData = SpUtlis.getLoginData(mActivity);
        final String account = loginData[0];
        final String password = loginData[1];
        final String regId = SpUtlis.getRegId(mActivity);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:appLogin('" + account + "','" + password + "','" + regId + "','" + data + "','" + code + "')");
            }
        }, 3000);
    }

    private void initListener() {
        //支付宝支付的回调监听
        alipayUtils.setOnPayCallBack(new AlipayUtils.OnPayCallBack() {
            @Override
            public void onPaySuccess() {

            }

            @Override
            public void onPayError(String code, String message) {

            }
        });

        //百度地图定位回调
        locationLogin.setOnLocationListener(new LocationUtils.OnLocationListener() {
            @Override
            public void onReceiveLocation(final BDLocation location) {
                locationLogin.stopLocation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String data = location.getProvince() + location.getCity() + location.getStreet();
                        final String cityCode = location.getCityCode();
                        SpUtlis.setLocationData(mActivity, cityCode, data);
                        SpUtlis.setDzLocationData(mActivity, cityCode, location.getCity());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isOne) {
                                    login(data, cityCode);
                                }
                                isOne = false;
                            }
                        });
                    }
                });
            }

            @Override
            public void onConnectHotSpotMessage(String s, final int i) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isOne) {
                            login("", "");
                        }
                        isOne = false;
                    }
                });


            }
        });

        locationGetData.setOnLocationListener(new LocationDataListener());

        main_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });

        /**
         * QQ分享的回调
         */
        shareDialog.setiUiListener(listener);

//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                startActivity(new Intent(mActivity, SettingActivity.class));
//                return true;
//            }
//        });
    }

    public void init() {
        if (!NetUtils.isNetworkAvailable(mActivity)) {
            linear.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }

        locationLogin = LocationUtils.getInstance(this);
        locationGetData = LocationUtils.getInstance(this);
        alipayUtils = new AlipayUtils(this);
        weiXinPayUtils = new WeiXinPayUtils(this, 0);
        wxapi = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        wxapi.registerApp(Constants.APP_ID);
        mTencent = Tencent.createInstance(Constants.TencentId, mActivity.getApplicationContext());
        listener = new MyIUiListener();

        locationLogin.startLocation();
        shareDialog = new ShareDialog(mActivity, webView);

        updateManager = new UpdateManager(mActivity);
        updateManager.isUpdate(true);

        //向sd卡中存入一张图片作为分享时使用
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File imageFile = new File(shareImagePath);
        if (imageFile.exists()) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.share_icon);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
//        SpUtlis.setLocationData(mActivity, "", "");
//        SpUtlis.setDzLocationData(mActivity, "", "");
        SpUtlis.setDz1LocationData(mActivity, "", "");
    }

    /**
     * 进入拍摄身份证界面
     */
    private void openCameraActivity()
    {
        startActivityForResult(new Intent(this.mActivity, CameraActivity.class), 5);
    }

    /**
     * 进行拍照
     */
    protected void getCameraPhoto() {

        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ybjk";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        long time = System.currentTimeMillis();
        String imageName = new SimpleDateFormat("yyMMddHHmmss").format(new Date(time));
        path = path + "/" + imageName + ".jpg";
        File imageFile = new File(path);
        Uri uri = Uri.fromFile(imageFile);

        //拍照
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 进行拍照
     */
    protected void getCameraPhoto(int index) {
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ybjk";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        long time = System.currentTimeMillis();
        String imageName = new SimpleDateFormat("yyMMddHHmmss").format(new Date(time));
        path = path + "/" + imageName + ".jpg";
        File imageFile = new File(path);
        Uri uri = Uri.fromFile(imageFile);

        //拍照
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);


        if (index == 2) {
            startActivityForResult(intent, PERSON_IDCARD_CAMERA);
        } else {
            startActivityForResult(intent, USER_PICK_FROM_CAMERA);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, listener);
        if (resultCode == RESULT_OK) {
            try {
                switch (requestCode) {
                    // 多选照片
                    case REQUEST_CAMERA_CODE:
                        photoList = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                        refreshsAdpater(photoList);
                        break;
                    // 拍照
                    case PICK_FROM_CAMERA:
                        ArrayList<String> list = new ArrayList<>();
                        //修改小米手机拍照后图片旋转的问题
                        String url = PhotoBitmapUtils.amendRotatePhoto(path, mActivity);
                        list.add(url);
                        refreshAdpater(list);
                        break;
                    // 单选照片
                    case REQUEST_CAMERA_SIGN_CODE:
                        refreshAdpater(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                        break;
                    // 拍照
                    case USER_PICK_FROM_CAMERA:
                        ArrayList<String> list1 = new ArrayList<>();
                        //修改小米手机拍照后图片旋转的问题
                        String url1 = PhotoBitmapUtils.amendRotatePhoto(path, mActivity);
                        list1.add(url1);
                        saveUserIcon(list1);
                        break;
                    // 单选照片
                    case USER_REQUEST_CAMERA_SIGN_CODE:
                        saveUserIcon(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                        break;
                    // 用户从相机拍摄到身份证
                    case PERSON_IDCARD_CAMERA:
                        ArrayList localArrayList1 = new ArrayList();
                        localArrayList1.add(data.getStringExtra("path"));
                        upLoadIdCard(localArrayList1);
                        break;
                    // 用户从相册选择身份证
                    case PERSON_IDCARD_PICK:
                        upLoadIdCard(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                        break;
                }
            } catch (Exception e) {

            }
        }else{
            webView.loadUrl("javascript:hideLoad()");
        }
    }

    /**
     * 上传身份证
     *
     * @param paths
     */
    private void upLoadIdCard(ArrayList<String> paths) {
        webView.loadUrl("javascript:showLoad()");
        String phone = SpUtlis.getLoginData(mActivity)[0];
        HttpUtils.getInstance().upLoadFile(mActivity, paths, phone, API.saveTempImg, 1500, new HttpUtils.OnOkHttpCallback() {
            @Override
            public void onSuccess(String body) {
                Log.e("body", body);
                try {
                    JSONObject object = new JSONObject(body);
                    webView.loadUrl("javascript:hideLoad()");
                    boolean b = object.optBoolean("result");
                    if (b) {
                        webView.loadUrl("javascript:singlePhoto('" + object.optJSONArray("data").optString(0) + "')");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Request error, Exception e) {

            }
        });
    }

    /**
     * 处理返回照片地址
     *
     * @param paths
     */
    private void refreshAdpater(final ArrayList<String> paths) {

        webView.loadUrl("javascript:showLoad()");
        upLodaImage(paths);
    }

    /**
     * 上传图片
     */
    private void upLodaImage(final List<String> paths) {
        HttpUtils.getInstance().upLoadFile(mActivity, paths, "", API.saveTempImg, new HttpUtils.OnOkHttpCallback() {
            @Override
            public void onSuccess(String body) {
                Log.e("body", body);
                try {
                    JSONObject object = new JSONObject(body);
                    JSONArray array = object.optJSONArray("data");
                    webView.loadUrl("javascript:hideLoad()");
                    boolean b = object.optBoolean("result");
                    if (b) {
                        webView.loadUrl("javascript:singlePhoto('" + array.optString(0) + "')");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Request error, Exception e) {

            }
        });
    }

    /**
     * 处理返回照片地址
     *
     * @param paths
     */
    private void saveUserIcon(final ArrayList<String> paths) {

        webView.loadUrl("javascript:showLoad()");
        String phone = SpUtlis.getLoginData(mActivity)[0];
        HttpUtils.getInstance().upLoadFile(mActivity, paths, phone, API.saveTempImg, new HttpUtils.OnOkHttpCallback() {
            @Override
            public void onSuccess(String body) {
                Log.e("body", body);
                try {
                    JSONObject object = new JSONObject(body);
                    webView.loadUrl("javascript:hideLoad()");
                    boolean b = object.optBoolean("result");
                    if (b) {
                        webView.loadUrl("javascript:singlePhoto('" + object.optJSONArray("data").optString(0) + "')");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Request error, Exception e) {

            }
        });
    }

    /**
     * 处理返回照片地址
     *
     * @param paths
     */
    private void refreshsAdpater(final ArrayList<String> paths) {

        webView.loadUrl("javascript:showLoad()");
        HttpUtils.getInstance().upLoadFile(mActivity, paths, "", API.saveTempImg, new HttpUtils.OnOkHttpCallback() {
            @Override
            public void onSuccess(String body) {
                Log.e("body", body);
                try {
                    JSONObject object = new JSONObject(body);
                    webView.loadUrl("javascript:hideLoad()");
                    boolean b = object.optBoolean("result");
                    if (b) {
                        webView.loadUrl("javascript:morePhoto(" + object.optJSONArray("data") + ")");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Request error, Exception e) {

            }
        });
    }

    private void show(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 接受EventBus 发送的登录的结果
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(LoginEvent event) {
        //微信登录是否成功，都回调这个方法
        webView.loadUrl("javascript:hideLoad()");
        if (event.isOk()) {
            //微信登录成功
            getAccessToken(event.getCode());
        }
    }

    /**
     * 微信登录成功后获取access_token
     *
     * @param code
     */
    public void getAccessToken(String code) {
        webView.loadUrl("javascript:hideLoad()");
        HttpUtils.getInstance().GET(mActivity,
                "https://api.weixin.qq.com/sns/oauth2/access_token"
                        + "?appid=" + Constants.APP_ID
                        + "&secret=" + Constants.AppSecret + "&code=" + code
                        + "&grant_type=authorization_code", new HttpUtils.OnOkHttpCallback() {
                    @Override
                    public void onSuccess(String body) {
                        try {
                            JSONObject object = new JSONObject(body);
                            //微信 登录成功，同时通知H5页面
                            String regId = SpUtlis.getRegId(mActivity);
                            String[] location = SpUtlis.getLocationData(mActivity);
                            String str = TextUtils.isEmpty(regId) ? "" : regId;
                            String openid = object.optString("openid");
                            webView.loadUrl("javascript:bindingPhone('WECHAT','','" + openid + "','" + str + "','" + location[1] + "','" + location[0] + "')");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Request error, Exception e) {

                    }
                });

    }

    /**
     * 重新加载界面
     */
    public void updateUI() {
        if (NetUtils.isNetworkAvailable(mActivity)) {
            webView.loadUrl(API.BASEURL);
            webView.setVisibility(View.VISIBLE);
            linear.setVisibility(View.GONE);
        } else {
            show("网络未连接，请点击重试");
        }
    }

    public class JavaInterface {

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
         * 打开相机拍摄照片（用户设置头像）
         */
        @JavascriptInterface
        public void openUserCamera() {
            getCameraPhoto(1);
        }

        /**
         * 打开相机拍摄身份证
         */
        @JavascriptInterface
        public void openUserIdCard() {
           MainActivity.this.openCameraActivity();
        }

        /**
         * 打开相册选取照片（身份证）
         */
        @JavascriptInterface
        public void openUserIdCardAlbum() {
            PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
            intent.setSelectModel(SelectModel.SINGLE);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            startActivityForResult(intent, USER_REQUEST_CAMERA_SIGN_CODE);
        }


        /**
         * 打开相册选取照片（单选）
         */
        @JavascriptInterface
        public void signOpenUserAlbum() {
            PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
            intent.setSelectModel(SelectModel.SINGLE);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            startActivityForResult(intent, USER_REQUEST_CAMERA_SIGN_CODE);
        }

        /**
         * 打开相机
         */
        @JavascriptInterface
        public void openCamera() {
            getCameraPhoto();
        }

        /**
         * 打开相册选取照片（多选）
         */
        @JavascriptInterface
        public void openAlbum(int count) {
            PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
            intent.setSelectModel(SelectModel.MULTI);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            intent.setMaxTotal(count); // 最多选择照片数量，默认为9
//            intent.setSelectedPaths(photoList); // 已选中的照片地址， 用于回显选中状态
            startActivityForResult(intent, REQUEST_CAMERA_CODE);
        }

        /**
         * 打开相册选取照片（单选）
         */
        @JavascriptInterface
        public void signOpenAlbum() {
            PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
            intent.setSelectModel(SelectModel.SINGLE);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            startActivityForResult(intent, REQUEST_CAMERA_SIGN_CODE);
        }

        /**
         * 开始定位
         */
        @JavascriptInterface
        public void startLoaction() {
            String[] location = SpUtlis.getDzLocationData(mActivity);
            if (!TextUtils.isEmpty(location[0])) {
                final JSONArray jsonArray = new JSONArray();
                try {
                    jsonArray.put(0, location[0]);
                    jsonArray.put(1, location[1]);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:getLocation(" + jsonArray + ")");
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("body", e.getMessage());
                }
            } else {
                locationGetData.startLocation();
            }
        }

        /**
         * 储存定位信息
         *
         * @param code
         * @param data
         */
        @JavascriptInterface
        public void setLocationData(String code, String data) {
            SpUtlis.setDz1LocationData(mActivity, code, data);
        }

        /**
         * 获取当前城市信息（不等于定位的城市信息）
         *
         * @return
         */
        @JavascriptInterface
        public String getCurentLocationData() {
            JSONArray array = new JSONArray();
            String[] locationData = SpUtlis.getDz1LocationData(mActivity);
            try {
                String cityCode = locationData[0];
                String data = locationData[1];
                if (!TextUtils.isEmpty(cityCode) || !TextUtils.isEmpty(data)) {
                    array.put(0, cityCode);
                    array.put(1, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String data;
            if (array.length() <= 0) {
                data = "";
            } else {
                data = array.toString();
            }
            return data;
        }

        /**
         * 储存用户登录信息 （账号密码）
         */
        @JavascriptInterface
        public void saveLoginData(String account, String password, String imagePath, boolean b) {
            SpUtlis.setLoginData(mActivity, account, password);
            SpUtlis.setSLoginData(mActivity, account, password, imagePath);
            if (b) {
                String regId = SpUtlis.getRegId(mActivity);
                String[] location = SpUtlis.getLocationData(mActivity);
                String str = TextUtils.isEmpty(regId) ? "" : regId;
                Map<String, String> params = new HashMap<>();
                params.put("userPhone", account);
                params.put("userPustCode", str);
                params.put("userLocation", location[1]);
                params.put("cityCode", location[0]);


                HttpUtils.getInstance().LoginPost(mActivity, params, API.saveBindingAppInfo, new HttpUtils.OnOkHttpCallback() {
                    @Override
                    public void onSuccess(String body) {
                        Log.e("body", body);
                        try {
                            JSONObject object = new JSONObject(body);
                            boolean result = object.optBoolean("result");
                            if (!result) {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Request error, Exception e) {

                    }
                });
            }
        }

        /**
         * 微信支付
         *
         * @param price
         */
        @JavascriptInterface
        public void weiXinPay(float price) {
            weiXinPayUtils.weiXinPay(price);
        }

        /**
         * 支付宝支付
         *
         * @param price
         */
        @JavascriptInterface
        public void aliPay(float price) {
            alipayUtils.topUpPay(price);
        }

        /**
         * 吊起分享功能
         */
        @JavascriptInterface
        public void share(final String url, final String title, final String content, final String imageUrl) {
            //将微信的类型变成 登录
            Constants.wxtype = Constants.WXSHARE;
            isShare = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shareDialog.showShare(url, title, content, imageUrl);
                }
            });
        }

        /**
         * 微信登录
         */
        @JavascriptInterface
        public void weiXinLogin() {
            //将微信的类型变成 登录
            Constants.wxtype = Constants.WXLOGIN;
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_zl_test";
            wxapi.sendReq(req);
        }

        /**
         * QQ登录
         */
        @JavascriptInterface
        public void qqLogin() {
            isShare = false;
            if (!mTencent.isSessionValid()) {
                mTencent.login(mActivity, "get_user_info", listener);
            }
        }

        /**
         * 应用退出（进入后台）
         */
        @JavascriptInterface
        public void appOut() {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mActivity.startActivity(intent);
                }
            });
        }

        /**
         * 应用退出（进入后台）
         */
        @JavascriptInterface
        public void getLoginData() {
            String[] sLoginData = SpUtlis.getSLoginData(mActivity);
            final String phone = sLoginData[0];
            final String password = sLoginData[1];
            final String url = sLoginData[2];
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(url)) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:loadUserInfoByAndroid('" + phone + "','" + password + "','" + url + "')");
                    }
                });
            }
        }

        /**
         * QQ退出登录
         */
        @JavascriptInterface
        public void qqLogout() {
            mTencent.logout(mActivity);
        }

        /**
         * 发送短信
         */
        @JavascriptInterface
        public void sendSms(final String phone) {
            SmsUtils.sendSms(phone, new SmscodeListener() {
                @Override
                public void getCodeSuccess(String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            show("验证码发送成功");
                        }
                    });
                }

                @Override
                public void getCodeFail(int i, String s) {

                }
            });

        }

        /**
         * 验证短信
         */
        @JavascriptInterface
        public void checkSmsCode(final String phone, final String code) {

            SmsUtils.checkSmsCode(phone, code, new SmscheckListener() {
                @Override
                public void checkCodeSuccess(final String code) {
                    // 验证码验证成功，code 为验证码信息。
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:getCheckData(" + 0 + ",'验证成功')");
                        }
                    });
                }

                @Override
                public void checkCodeFail(int errCode, final String errMsg) {
                    // 验证码验证失败, errCode 为错误码，详情请见文档后面的错误码表；errMsg 为错误描述。
                    final String errData;
                    switch (errCode) {
                        case 4015:
                            errData = "验证码不正确";
                            break;
                        case 4016:
                            errData = "没有余额";
                            break;
                        case 4017:
                            errData = "验证码超时";
                            break;
                        case 4018:
                            errData = "重复验证";
                            break;
                        case 2997:
                            errData = "未获取验证码";
                            break;
                        default:
                            errData = "验证失败";
                            break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:getCheckData(" + 1 + ",'" + errData + "')");
                        }
                    });
                }
            });
        }

        /**
         * 清空登录账号和密码
         */
        @JavascriptInterface
        public void loginOut() {
            SpUtlis.setLoginData(mActivity, "", "");
        }

        /**
         * 清空照片
         */
        @JavascriptInterface
        public void clearPhotos() {
            photoList.clear();
        }

        /**
         * app检测更新
         */
        @JavascriptInterface
        public void checkUpdate() {
            updateManager.isUpdate(false);
        }

        /**
         * 获取当前版本名称
         */
        @JavascriptInterface
        public String getVersionName() {
            //获取当前APP的版本号
            try {
                final PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
                return packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return "";
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
            MainActivity.this.finish();
        }
    }

    /**
     * QQ登录的回调接口
     */
    public class MyIUiListener implements IUiListener {
        @Override
        public void onComplete(Object o) {
            webView.loadUrl("javascript:hideLoad()");
            try {
                if (isShare) {
                    //QQ分享成功，同时通知H5页面
                    webView.loadUrl("javascript:forwardSaveQQ(" + true + ")");
                } else {
                    //QQ登录成功，同时通知H5页面
                    JSONObject object = new JSONObject(o.toString());
                    String regId = SpUtlis.getRegId(mActivity);
                    String[] location = SpUtlis.getLocationData(mActivity);
                    String str = TextUtils.isEmpty(regId) ? "" : regId;
                    webView.loadUrl("javascript:bindingPhone('QQ','" + object.optString("openid") + "','','" + str + "','" + location[1] + "','" + location[0] + "')");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(UiError uiError) {
            webView.loadUrl("javascript:hideLoad()");
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isShare) {
                        //QQ分享失败，同时通知H5页面
                        webView.loadUrl("javascript:forwardSaveQQ(" + false + ")");
                    } else {
                        webView.loadUrl("javascript:hideLoad()");
                    }
                }
            });
        }

        @Override
        public void onCancel() {

        }
    }

    /**
     * 提供定位数据
     */
    private class LocationDataListener implements LocationUtils.OnLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            locationGetData.stopLocation();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String data = location.getProvince() + location.getCity() + location.getStreet();
                    final String cityCode = location.getCityCode();
                    SpUtlis.setLocationData(mActivity, cityCode, data);
                    SpUtlis.setDzLocationData(mActivity, cityCode, location.getCity());
                    final JSONArray jsonArray = new JSONArray();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jsonArray.put(0, cityCode);
                                jsonArray.put(1, location.getCity());
                                webView.loadUrl("javascript:getLocation(" + jsonArray + ")");
                            } catch (Exception e) {
                                android.util.Log.e("body", e.getMessage());
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:getLocation('')");
                }
            });
        }
    }

}
