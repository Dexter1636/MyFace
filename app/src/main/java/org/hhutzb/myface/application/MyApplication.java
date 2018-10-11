package org.hhutzb.myface.application;

import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;


public class MyApplication extends MultiDexApplication {

    private static MyApplication instance;

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Stetho初始化
        Stetho.initializeWithDefaults(this);
    }

}
