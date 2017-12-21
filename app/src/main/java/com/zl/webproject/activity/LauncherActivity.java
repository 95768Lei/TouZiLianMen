package com.zl.webproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.zl.webproject.R;
import com.zl.webproject.utils.SpUtlis;

/**
 * @author zhanglei
 * @date 17/8/6
 * 启动页
 */
public class LauncherActivity extends Activity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        SpUtlis.setDz1LocationData(this, "", "");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PackageInfo pkg = null;
                try {
                    pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
                    int versionCode = SpUtlis.getVersionCode(LauncherActivity.this);
                    if (versionCode == -1) {
                        //储存当前版本号
                        SpUtlis.setVersionCode(LauncherActivity.this, pkg.versionCode);
                        //进入欢迎页
                        startActivity(new Intent(LauncherActivity.this, WelComeActivity.class));
                    } else {
                        if (versionCode != pkg.versionCode) {
                            //储存当前版本号
                            SpUtlis.setVersionCode(LauncherActivity.this, pkg.versionCode);
                            //进入欢迎页
                            startActivity(new Intent(LauncherActivity.this, WelComeActivity.class));
                        } else {
                            //进入首页
                            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                        }
                    }

                    finish();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }, 1500);
    }
}
