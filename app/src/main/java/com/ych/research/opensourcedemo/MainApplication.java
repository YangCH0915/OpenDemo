package com.ych.research.opensourcedemo;

import android.app.Application;
import android.content.Context;

import okhttp3.Request;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class MainApplication extends Application {

    public static Context mContext;

    private Request request;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }


    public Request getRequestInstance() {
        if (request == null) {
            request = new Request.Builder().build();
        }
        return request;
    }
}
