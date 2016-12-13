package com.yao.wechatpaydemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.review.signature.Review;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button wechat;
    private Button aply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wechat = (Button) findViewById(R.id.btn_wechat);
        wechat.setOnClickListener(this);
        aply = (Button) findViewById(R.id.btn_aply);
        aply.setOnClickListener(this);
        Review.MD5Review(this, "com.yao.wechatpaydemo", "3238cab38af092f76774cf8654550cbb");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wechat:
                startActivity(new Intent(MainActivity.this, WxPayActivity.class));
                break;
            case R.id.btn_aply:
                break;
        }
    }
}
