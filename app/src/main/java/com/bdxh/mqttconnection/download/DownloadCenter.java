package com.bdxh.mqttconnection.download;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bdxh.mqttconnection.BaseActivity;
import com.bdxh.mqttconnection.MoreBaseUrlInterceptor;
import com.blankj.utilcode.util.LogUtils;
import java.io.File;
import java.io.IOException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 下载中心
 */
public class DownloadCenter {
    private static final String TAG = "rustAppDownloadCenter";

    private static DownloadCenter instance;

    private static Retrofit retrofit;
    private static BaseActivity context;

    private List<ControlCallBack> callBackList = new ArrayList<>();
    private Set<DownloadCenterListener> listeners = new HashSet<>();

    private DownloadCenter() {
        init();
    }

    public static DownloadCenter getInstance(BaseActivity activity) {
        if (instance == null) {
            synchronized (DownloadCenter.class) {
                if (instance == null) {
                    instance = new DownloadCenter();
                    context = activity ;
                }
            }
        }
        return instance;
    }

    public void continueDownload(final ControlCallBack callBack) {
        if (!callBackList.contains(callBack)) {
            Log.e(TAG, "continueDownload: not found: " + callBack);
            return;
        }
        switch (callBack.getState()) {
            case CREATED:
            case PAUSED:
            case ERROR:
                break;
            default:
                return;
        }
        Log.d(TAG, "continueDownload: tmpFile: " + callBack.getTmpFile());
        long startByte = 0;
        if (callBack.getTmpFile().exists()) {
            startByte = callBack.getTmpFile().length();
            Log.d(TAG, "continueDownload: tmp file exists.");
        } else {
            try {
                boolean c =callBack.getTmpFile().createNewFile();
                Log.d(TAG, "continueDownload: Create new tmp file: " + c);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "continueDownload: Create new tmp file. ", e);
            }
        }
        Log.d(TAG, "continueDownload: startByte: " + startByte);
        callBack.setLocalFileStartByteIndex(startByte);
        tellDownloadStart(callBack);
        retrofit.create(ApiService.class)
                .downloadPartial(callBack.getUrl(), "bytes=" + startByte + "-")
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .doOnNext(responseBody -> {
                    LogUtils.d(" accept success responseBody : " + responseBody);
                    callBack.saveFile(responseBody);
                })
                .doOnError(throwable -> {
                    LogUtils.d(" accecpt error msg : " +throwable.getMessage());
                    tellDownloadError(callBack.getUrl(), throwable);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callBack.setState(DownloadTaskState.ERROR);
                        tellDownloadError(callBack.getUrl(), e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void download(final String downUrl, File targetFile, final int downloadBytePerMs) {
        ControlCallBack callBack = null;
        for (ControlCallBack c : callBackList) {
            if (c.getUrl().equals(downUrl)) {
                callBack = c;
                break;
            }
        }
        if (callBack == null) {
            callBack = new ControlCallBack(downUrl, targetFile, downloadBytePerMs) {
                @Override
                public void onSuccess(String url) {
                    tellDownloadSuccess(url);
                }

                @Override
                public void onPaused(String url) {
                    tellDownloadPaused(url);
                }

                @Override
                public void onError(String url, Throwable e) {
                    tellDownloadError(url, e);
                }

                @Override
                public void onDelete(String url) {
                    for (ControlCallBack c : callBackList) {
                        if (url.equals(c.getUrl())) {
                            callBackList.remove(c);
                            break;
                        }
                    }
                    tellDownloadDelete(url);
                }
            };
            callBackList.add(callBack);
        }
        if (callBack.isDownloading()) {
            LogUtils.d( "downloading this task.");
            return;
        }

        tellDownloadStart(callBack);
        final ControlCallBack finalCallBack = callBack;
        retrofit.create(ApiService.class)
                .download(downUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .compose(ProgressUtils.applyProgressBar(context))
                .doOnNext(responseBody -> {
                    LogUtils.d(" accept success responseBody : " + responseBody);
                    finalCallBack.saveFile(responseBody);
                })
                .doOnError(throwable -> {
                    LogUtils.e( "accept on error: " + downUrl, throwable);
                    finalCallBack.onError(finalCallBack.getUrl(),throwable);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        finalCallBack.setState(DownloadTaskState.ERROR);
                        tellDownloadError(downUrl, e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(new MoreBaseUrlInterceptor())
                .addInterceptor(new ProgressInterceptor((url, bytesRead, contentLength, done) -> {
                    tellProgress(url, bytesRead, contentLength, done);
                    double progress = bytesRead * 1.0 / contentLength;
                    LogUtils.d(" Okhttp 下载进度 "+ progress);
                }))
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)

                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://yourbaseurl.com")
                .build();
    }


    public interface ApiService {

        String base_url = "http://172.0.0.92:8080/";
        String base_url_mdffx = "http://11.254.16.19/";

        @Streaming  //该属性防止大文件写入内存
        @GET
        Observable<ResponseBody> download(@Url String url);

        @Streaming
        @GET
        Observable<ResponseBody> downloadPartial(@Url String url, @Header("Range") String range);

    }


    public void addListener(DownloadCenterListener l) {
        listeners.add(l);
    }

    public void removeListener(DownloadCenterListener l) {
        listeners.remove(l);
    }

    private void tellDownloadSuccess(String url) {
        for (DownloadCenterListener l : listeners) {
            l.onSuccess(url);
        }
    }

    private void tellDownloadPaused(String url) {
        for (DownloadCenterListener l : listeners) {
            l.onPaused(url);
        }
    }

    private void tellDownloadError(String url, Throwable e) {
        for (DownloadCenterListener l : listeners) {
            l.onError(url, e);
        }
    }

    private void tellProgress(String url, long bytesRead, long contentLength, boolean done) {
        for (DownloadCenterListener l : listeners) {
            l.onProgress(url, bytesRead, contentLength, done);
        }
    }

    private void tellDownloadDelete(String url){
        for (DownloadCenterListener l : listeners){
            l.onDeleted(url);
        }
    }

    private void tellDownloadStart(ControlCallBack callBack){
        for (DownloadCenterListener l : listeners){
            l.onStart(callBack);
        }
    }
}

