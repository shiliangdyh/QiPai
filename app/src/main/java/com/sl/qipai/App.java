package com.sl.qipai;

import android.app.Application;

import cn.bmob.v3.Bmob;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "c0770bc4b8769ce9bf873e6b306e9054");
    }
}
