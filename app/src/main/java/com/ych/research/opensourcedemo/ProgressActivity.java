package com.ych.research.opensourcedemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ProgressActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private String url = "http://music.baidu.com/song/242137130";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.button_download:
                DownloadManager.getInstance().download(url, new DownloadObserver() {

                    DownloadInfo downloadInfo;

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        this.downloadInfo = downloadInfo;
                        progressBar.setMax((int) downloadInfo.getTotal());
                        progressBar.setProgress((int) downloadInfo.getProgress());
                    }

                    @Override
                    public void onComplete() {
                        if (downloadInfo != null) {
                            Toast.makeText(ProgressActivity.this, downloadInfo.getFileName() + Uri.encode("下载完成"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case R.id.btn_cancel:
                DownloadManager.getInstance().cancel(url);
                break;
        }
    }
}
