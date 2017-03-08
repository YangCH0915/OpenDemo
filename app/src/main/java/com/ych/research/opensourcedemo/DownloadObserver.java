package com.ych.research.opensourcedemo;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public abstract class DownloadObserver implements Observer<DownloadInfo> {

    protected Disposable d;
    protected DownloadInfo di;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.di = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}
