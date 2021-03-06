package com.bdxh.mqttconnection.download;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.bdxh.mqttconnection.BaseActivity;
import com.bdxh.mqttconnection.R;

import java.io.File;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import butterknife.BindView;

public class DownloadActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.download_1)
    Button download1;
    @BindView(R.id.download_2)
    Button download2;
    @BindView(R.id.download_3)
    Button download3;
    @BindView(R.id.container)
    FrameLayout container;

    private static final int REQ_CODE = 17;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_install_app;
    }

    @Override
    protected void init() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
        setOnClick(this, R.id.download_1, R.id.download_2, R.id.download_3);
        getSupportFragmentManager().beginTransaction().add(R.id.container, new DownloadingFrag()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请允许应用读写外部存储", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // 下载前应该检查存储中是否已经有这个文件
        switch (v.getId()) {
            case R.id.download_1:
                final String url1 = "http://dldir1.qq.com/weixin/android/weixin707android1520.apk";
                downloadUrl(url1, new File(Environment.getExternalStorageDirectory(), "weixin707android1520.apk"), 2000);
                break;
            case R.id.download_2:
                final String url2 = "http://releases.ubuntu.com/18.04.3/ubuntu-18.04.3-desktop-amd64.iso?_ga=2.164765245.385568095.1571216179-1901711613.1571216179";
                downloadUrl(url2, new File(Environment.getExternalStorageDirectory(), "ubuntu-18.04.3-desktop-amd64.iso"), 1000);
                break;
            case R.id.download_3:
                final String url3 = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk";
                downloadUrl(url3, new File(Environment.getExternalStorageDirectory(), "alipay_wap_main.apk"), 1000000);
                break;
        }
    }

    private void downloadUrl(final String url, File targetFile, final int downloadBytePerMs) {
        DownloadCenter.getInstance(this).download(url, targetFile, downloadBytePerMs);
    }

    private void setOnClick(View.OnClickListener l, int... id) {
        for (int i : id) {
            findViewById(i).setOnClickListener(l);
        }
    }

}
