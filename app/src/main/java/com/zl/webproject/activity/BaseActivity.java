package com.zl.webproject.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhy.autolayout.AutoLayoutActivity;

/**
 * Created by zhanglei on 2017/3/24 0024.
 * Activity基类
 */

public class BaseActivity extends AutoLayoutActivity {

    protected Activity mActivity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
    }

    protected void show(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置界面透明度
     *
     * @param alpha
     */
    protected void setAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }
}
