package com.example.steven.sjtu_lib_v2.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.steven.sjtu_lib_v2.QRCodeUtil;
import com.example.steven.sjtu_lib_v2.R;

public class RoboWatcherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robo_watcher);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        String task=bundle.getString("task");
        ImageView mImageView = (ImageView) findViewById(R.id.qrcode);
        Bitmap mBitmap = QRCodeUtil.createQRCodeBitmap(task, 480, 480);
        mImageView.setImageBitmap(mBitmap);
    }
}
