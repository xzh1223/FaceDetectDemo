package com.gst.gstfacedemo;

import android.app.Application;
import com.lzy.okgo.OkGo;

/**
 * Created by alanzhou on 17-4-14.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //必须调用初始化
        OkGo.init(this);
    }
}
