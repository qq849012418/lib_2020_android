package com.example.steven.sjtu_lib_v2.activity;


import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by zhoujian on 2018/1/19.
 */

public class myActivityManager {

    private static myActivityManager sInstance = new myActivityManager();

    private WeakReference<SingleDetailActivity> sCurrentActivityWeakRef;


    private myActivityManager() {

    }

    public static myActivityManager getInstance() {
        return sInstance;
    }

    public SingleDetailActivity getCurrentActivity() {
        SingleDetailActivity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(SingleDetailActivity activity) {
        sCurrentActivityWeakRef = new WeakReference<SingleDetailActivity>(activity);
    }

}

