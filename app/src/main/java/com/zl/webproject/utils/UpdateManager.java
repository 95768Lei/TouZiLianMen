package com.zl.webproject.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;

import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Created by zhanglei on 2017/4/19.
 * 软件更新的管理类
 */

public class UpdateManager {

    private Activity context;
    private final ProgressDialog dialog;
    private final AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private String uploadUrl;
    //    private VersionBean versionBean;

    public UpdateManager(final Activity context) {
        this.context = context;
        dialog = new ProgressDialog(context);
        dialog.setTitle("程序更新中...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCanceledOnTouchOutside(false);

        builder = new AlertDialog.Builder(context);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startUpdate();
            }
        });
    }

    /**
     * 判断APP是否需要更新
     *
     * @return
     */
    public void isUpdate(final boolean isMain) {

        try {
            //获取当前APP的版本号
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            HttpUtils.getInstance().GET(context, API.upload, new HttpUtils.OnOkHttpCallback() {

                @Override
                public void onSuccess(String body) {
                    try {
                        JSONObject object = new JSONObject(body);
                        boolean result = object.optBoolean("result");
                        if (result) {
                            JSONObject data = object.optJSONObject("data");
                            uploadUrl = data.optString("uploadUrl");
                            if (data.optInt("uploadCode") > packageInfo.versionCode) {
                                builder.setMessage(data.optString("uploadContext"));
                                alertDialog = builder.create();
                                alertDialog.show();
                            } else {
                                if (!isMain) {
                                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            if (!isMain) {
                                Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Request error, Exception e) {

                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始更新
     */
    private void startUpdate() {
        dialog.show();
        HttpUtils.getInstance().download(API.BASEURL + uploadUrl, Environment.getExternalStorageDirectory().getAbsolutePath() + "/tzlm/", new HttpUtils.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(final String filePath) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        install(filePath);
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgress(progress);
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 安装 APK
     */
    private void install(String mUrl) {
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(mUrl)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
