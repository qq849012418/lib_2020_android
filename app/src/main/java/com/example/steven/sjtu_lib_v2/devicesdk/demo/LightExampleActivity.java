package com.example.steven.sjtu_lib_v2.devicesdk.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.example.steven.sjtu_lib_v2.R;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 注意！！！！
 * 1.该示例只共快速接入使用，只适用于有 Status、Data属性的快速接入测试设备；
 * 2.真实设备可以参考 ControlPanelActivity 里面有数据上下行示例；
 */
public class LightExampleActivity extends BaseActivity {

    private final static int REPORT_MSG = 0x100;

    TextView consoleTV;
    String consoleStr;
    private InternalHandler mHandler = new InternalHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在初始化的时候可以设置 灯的初始状态，或者等初始化完成之后 上报一次设备所有属性的状态
        // 注意在调用云端接口之前确保初始化完成了
        setContentView(R.layout.activity_light_example);
        consoleTV = (TextView) findViewById(R.id.textview_console);
        setDownStreamListener();
        showToast("已启动每5秒上报一次状态");
        log("已启动每5秒上报一次状态");
        mHandler.sendEmptyMessageDelayed(REPORT_MSG, 2 * 1000);
    }

    /**
     * 数据上行
     * 上报灯的状态
     */
    public void reportHelloWorld() {
        log("上报 Hello, World！");
        try {
            Map<String, ValueWrapper> reportData = new HashMap<>();
            //reportData.put("Status", new ValueWrapper.BooleanValueWrapper(1)); // 1开 0 关
            //reportData.put("Data", new ValueWrapper.StringValueWrapper("Hello, World!")); //
            String tasklist="";
            String name = "test";
            String path = "C300";
            String code = "I313.45/24-3 2019";
            for(int i=0;i<2;i++){
                tasklist+="{\"bookname\":\""+name+i+"\"," +
                        "\"path\":\""+path+"\"," +
                        "\"code\":\""+code+"\"}\n";


            }
            reportData.put("data",new ValueWrapper.StringValueWrapper(tasklist));
            reportData.put("Uab",new ValueWrapper.IntValueWrapper(1234));
            LinkKit.getInstance().getDeviceThing().thingPropertyPost(reportData, new IPublishResourceListener() {
                @Override
                public void onSuccess(String s, Object o) {
                    Log.d(TAG, "onSuccess() called with: s = [" + s + "], o = [" + o + "]");
                    showToast("设备上报状态成功");
                    log("上报 Hello, World! 成功。");
                }

                @Override
                public void onError(String s, AError aError) {
                    Log.d(TAG, "onError() called with: s = [" + s + "], aError = [" + aError + "]");
                    showToast("设备上报状态失败");
                    log("上报 Hello, World! 失败。");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据上行
     * 上报图书馆用户自定义消息
     */
    public void reportCustom() {
        log("上报custom！");
        // 发布
        MqttPublishRequest request = new MqttPublishRequest();
        request.isRPC = false;
// topic 替换成用户自己需要发布的 topic
        request.topic = "/a18ZNg4wyLC/libapp/user/update?_sn=default";
//// 设置 qos
//        request.qos = 0;
// data 替换成用户需要发布的数据 json String
//示例 属性上报 {"id":"160865432","method":"thing.event.property.post","params":{"LightSwitch":1},"version":"1.0"}
        request.payloadObj = "{\"test1\":\"test2\"}";
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                // 发布成功
                log("上报custom成功。");
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                // 发布失败
            }
        });
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
                    String result = new String((byte[]) aMessage.data, "UTF-8");
                    RequestModel<String> receiveObj = JSONObject.parseObject(result, new TypeReference<RequestModel<String>>() {
                    }.getType());
                    log("Received raw: "+result);
                    log("Received a message: " + (receiveObj==null?"":receiveObj.params));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public boolean shouldHandle(String s, String s1) {
            Log.d(TAG, "shouldHandle() called with: s = [" + s + "], s1 = [" + s1 + "]");
            return true;
        }

        @Override
        public void onConnectStateChange(String s, ConnectState connectState) {
            Log.d(TAG, "onConnectStateChange() called with: s = [" + s + "], connectState = [" + connectState + "]");
        }
    };

    private void log(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ALog.d(TAG, "log(), " + str);
                if (TextUtils.isEmpty(str))
                    return;
                consoleStr = consoleStr + "\n \n" + (getTime()) + " " + str;
                consoleTV.setText(consoleStr);
            }
        });

    }

    private void clearMsg() {
        consoleStr = "";
        consoleTV.setText(consoleStr);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeMessages(REPORT_MSG);
            mHandler.removeCallbacksAndMessages(null);
            showToast("停止定时上报");
        }
        LinkKit.getInstance().unRegisterOnPushListener(notifyListener);
        clearMsg();
    }

    private class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case REPORT_MSG:
                    reportHelloWorld();
                    mHandler.sendEmptyMessageDelayed(REPORT_MSG, 5*1000);
                    break;
            }

        }
    }

}
