package com.zl.webproject.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.platform.comapi.map.B;
import com.blankj.utilcode.util.SPUtils;

/**
 * Created by Administrator on 2017/8/16.
 */

public class API {

    public static String BASEURL = "http://app.tzlm.cc";
//    public static String BASEURL = "http://172.16.18.20:8888/CAR";
    public static String shareLogo = BASEURL + "/img/logo.png";
    public static String LOGIN = BASEURL + "/app/user/appLogin.do";
    public static String saveBindingAppInfo = BASEURL + "/app/user/saveBindingAppInfo.do";
    public static String saveTempImg = BASEURL + "/app/file/saveTempImg.do";
    public static String delTempImg = BASEURL + "/app/file/delTempImg.do";
    public static String upload = BASEURL + "/app/upload/appUpload.do";
    public static final String image_file_path = Environment.getExternalStorageDirectory().getPath() + "/JavaCV_Image/";
    //拍摄完成后的照片
    public static final String image_path_name = image_file_path + "/image.JPG";
    public static void init(Context context) {
        BASEURL = SpUtlis.getUrl(context);
        LOGIN = BASEURL + "/app/user/appLogin.do";
        saveBindingAppInfo = BASEURL + "/app/user/saveBindingAppInfo.do";
        saveTempImg = BASEURL + "/app/file/saveTempImg.do";
        delTempImg = BASEURL + "/app/file/delTempImg.do";
        upload = BASEURL + "/app/upload/appUpload.do";
    }


    public static String editUserImg = BASEURL + "/app/user/editUserImg.do";

}
