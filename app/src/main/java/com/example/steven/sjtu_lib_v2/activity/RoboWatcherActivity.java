package com.example.steven.sjtu_lib_v2.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.example.steven.sjtu_lib_v2.QRCodeUtil;
import com.example.steven.sjtu_lib_v2.R;

import java.io.UnsupportedEncodingException;

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

        setDownStreamListener();


    }
    private void setDownStreamListener(){
        LinkKit.getInstance().registerOnPushListener(notifyListener);
    }

    private IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String s, String s1, AMessage aMessage) {
            try {
//                if (s1 != null && s1.contains("service/property/set")) {
                if (s1 != null && s1.contains("service/property/set")) {
                    final String result = new String((byte[]) aMessage.data, "UTF-8");
                    RequestModel<String> receiveObj = com.alibaba.fastjson.JSONObject.parseObject(result, new TypeReference<RequestModel<String>>() {
                    }.getType());
                    Log.v("msg","Received raw: "+result);
                    Log.v("msg","Received a message: " + (receiveObj==null?"":receiveObj.params));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                        }
                    });
                    //此处假设数据为{"data":{
                    //if(result.)
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public boolean shouldHandle(String s, String s1) {
            Log.v("msg", "shouldHandle() called with: s = [" + s + "], s1 = [" + s1 + "]");
            return true;
        }

        @Override
        public void onConnectStateChange(String s, ConnectState connectState) {
            Log.v("msg","onConnectStateChange() called with: s = [" + s + "], connectState = [" + connectState + "]");
        }
    };
}
