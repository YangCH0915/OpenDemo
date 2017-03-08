package com.ych.research.opensourcedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class DownloadManager {

    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();

    private HashMap<String, Call> downCalls;

    private OkHttpClient mClient;

    private DownloadManager() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    public static DownloadManager getInstance() {
        for (; ; ) {
            DownloadManager crrent = INSTANCE.get();
            if (crrent != null) {
                return crrent;
            }
            crrent = new DownloadManager();
            if (INSTANCE.compareAndSet(null, crrent)) {
                return crrent;
            }
        }
    }

    public void download(String url, DownloadObserver downloadObserver) {
        Observable.just(url)
                .filter(s -> !downCalls.containsKey(s))
                .flatMap(s -> Observable.just(createDownloadInfo(s)))
                .map(this::getRealFileName)
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(downloadObserver);
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();
        }
        downCalls.remove(url);
    }

    private DownloadInfo createDownloadInfo(String url) {
        DownloadInfo info = new DownloadInfo(url);
        long contentLength = getContentLength(url);
        info.setTotal(contentLength);
        String fileName = url.substring(url.lastIndexOf("/"));
        info.setFileName(fileName);
        return info;
    }

    private long getContentLength(String url) {
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DownloadInfo.TOTAL_ERROR;
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        File file = new File(MainApplication.mContext.getFilesDir(), fileName);
        if (file.exists()) {
            downloadLength = file.length();
        }
        //之前下载过,需要重新来一个文件
        int i = 1;
        while (downloadLength >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String fileNameOther;
            if (dotIndex == -1) {
                fileNameOther = fileName + "(" + i + ")";
            } else {
                fileNameOther = fileName.substring(0, dotIndex)
                        + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(MainApplication.mContext.getFilesDir(), fileNameOther);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {

        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();

            long downloadLength = downloadInfo.getProgress();
            long contentLength = downloadInfo.getTotal();
            e.onNext(downloadInfo);
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);
            Response response = call.execute();

            File file = new File(MainApplication.mContext.getFilesDir(), downloadInfo.getFileName());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                downCalls.remove(url);
            } finally {
                IoUtils.closeAll(is, fileOutputStream);
            }
            e.onComplete();
        }
    }

}














