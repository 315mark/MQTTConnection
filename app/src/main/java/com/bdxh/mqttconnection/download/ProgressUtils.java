package com.bdxh.mqttconnection.download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.bdxh.mqttconnection.BaseActivity;

import java.lang.ref.WeakReference;
import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class ProgressUtils {

    public static <T> ObservableTransformer<T, T> applyProgressBar(
            @NonNull final BaseActivity activity, String msg ) {
        final WeakReference<BaseActivity> activityWeakReference = new WeakReference<>(activity);
        final ProgressDialog dialogUtils = new ProgressDialog(activity);
        dialogUtils.show();
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).doOnTerminate(new Action(){
                    @Override
                    public void run() throws Exception {
                        Activity context;
                        if ((context = activityWeakReference.get()) != null
                                && !context.isFinishing()) {
                            dialogUtils.dismiss();
                        }
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        /*Activity context;
                        if ((context = activityWeakReference.get()) != null
                                && !context.isFinishing()) {
                            dialogUtils.dismissProgress();
                        }*/
                    }
                });
            }
        };
    }

    public static <T> ObservableTransformer<T, T> applyProgressBar(
            @NonNull final BaseActivity activity) {
        return applyProgressBar(activity, "");
    }
}
