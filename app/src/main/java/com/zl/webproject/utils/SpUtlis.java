package com.zl.webproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhanglei on 2017/8/6.
 */

public class SpUtlis {

    public static void setUrl(Context context, String url) {
        SharedPreferences sp = context.getSharedPreferences("url", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("url", url);
        edit.commit();
    }

    public static String getUrl(Context context) {
        SharedPreferences sp = context.getSharedPreferences("url", Context.MODE_PRIVATE);
        return sp.getString("url", "http://app.tzlm.cc");
    }

    public static void setVersionCode(Context context, int versionCode) {
        SharedPreferences sp = context.getSharedPreferences("version", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("versionCode", versionCode);
        edit.commit();
    }

    public static int getVersionCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences("version", Context.MODE_PRIVATE);
        return sp.getInt("versionCode", -1);
    }

    public static void setRegId(Context context, String regId) {
        SharedPreferences sp = context.getSharedPreferences("regId", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("regId", regId);
        edit.commit();
    }

    public static String getRegId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("regId", Context.MODE_PRIVATE);
        return sp.getString("regId", "");
    }

    public static void setLoginData(Context context, String account, String password) {
        SharedPreferences sp = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("account", account);
        edit.putString("password", password);
        edit.commit();
    }

    public static String[] getLoginData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String account = sp.getString("account", "");
        String password = sp.getString("password", "");

        String[] users = {account, password};

        return users;
    }

    public static void setSLoginData(Context context, String account, String password, String url) {
        SharedPreferences sp = context.getSharedPreferences("users", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("account", account);
        edit.putString("password", password);
        edit.putString("url", url);
        edit.commit();
    }

    public static String[] getSLoginData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("users", Context.MODE_PRIVATE);
        String account = sp.getString("account", "");
        String password = sp.getString("password", "");
        String url = sp.getString("url", "");

        String[] users = {account, password, url};

        return users;
    }

    public static void setLocationData(Context context, String cityCode, String data) {
        SharedPreferences sp = context.getSharedPreferences("location", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("cityCode", cityCode);
        edit.putString("data", data);
        edit.commit();
    }

    public static String[] getLocationData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("location", Context.MODE_PRIVATE);
        String account = sp.getString("cityCode", "");
        String password = sp.getString("data", "");

        String[] loactions = {account, password};

        return loactions;
    }

    public static void setDzLocationData(Context context, String cityCode, String data) {
        SharedPreferences sp = context.getSharedPreferences("dz_location", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("cityCode", cityCode);
        edit.putString("data", data);
        edit.commit();
    }

    public static String[] getDzLocationData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("dz_location", Context.MODE_PRIVATE);
        String account = sp.getString("cityCode", "");
        String password = sp.getString("data", "");
        String[] loactions = {account, password};
        return loactions;
    }

    public static void setDz1LocationData(Context context, String cityCode, String data) {
        SharedPreferences sp = context.getSharedPreferences("dz1_location", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("cityCode", cityCode);
        edit.putString("data", data);
        edit.commit();
    }

    public static String[] getDz1LocationData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("dz1_location", Context.MODE_PRIVATE);
        String account = sp.getString("cityCode", "");
        String password = sp.getString("data", "");
        String[] loactions = {account, password};
        return loactions;
    }
}
