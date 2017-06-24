package com.yandongqi.dnote.app;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by XhinLiang on 2017/5/13.
 * xhinliang@gmail.com
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "p9JckjzxvdF3eyegDawrhmT3-gzGzoHsz", "4TdIF71ebcIWWKEtxzDr4Jva");
    }
}
