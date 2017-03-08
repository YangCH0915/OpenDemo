package com.ych.research.opensourcedemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class MainApplication extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }
}
