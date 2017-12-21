package com.zl.webproject;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.sms.SMSSDK;

/**
 * Created by zhanglei on 2017/3/30 0030.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        //初始化JPush
        JPushInterface.init(this);
        //初始化JPush SMSSDK
        SMSSDK.getInstance().initSdk(this);
        //设置JPush前后两次获取验证码的时间间隔
        SMSSDK.getInstance().setIntervalTime(60000);
        //初始化百度地图
        SDKInitializer.initialize(getApplicationContext());
    }

}
