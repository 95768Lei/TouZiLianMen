package com.zl.webproject.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zl.webproject.R;
import com.zl.webproject.dialog.ShareDialog;
import com.zl.webproject.utils.SpUtlis;

/**
 * @author zhanglei
 * @date 17/8/6
 * 设置连接地址
 */
public class SettingActivity extends Activity {

    private TextView textUrl;
    private EditText etNewUrl;
    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        textUrl = (TextView) findViewById(R.id.text_current_url);
        etNewUrl = (EditText) findViewById(R.id.et_new_url);

        textUrl.setText("当前地址是：" + SpUtlis.getUrl(this));
        etNewUrl.setText(SpUtlis.getUrl(this));
        shareDialog = new ShareDialog(this, textUrl);
    }

    /**
     * 设置新地址
     *
     * @param view
     */
    public void settingUrl(View view) {
        try {
            SpUtlis.setUrl(this, etNewUrl.getText().toString().trim());
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {

        }
    }

    public void sendSms(View view) {
        shareDialog.showShare("http://www.baidu.com", "title", "content", "http://wx2.sinaimg.cn/thumbnail/64d9dc78ly1fj529rml68j20p90kstcd.jpg");
//        ShareUtils.showShare(this,"http://www.baidu.com", "title", "content", "https://image.3761.com/pic/66871399945977.png");
    }
}
