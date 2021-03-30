package com.bdxh.mqttconnection;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 基础Activity   封装RxBinding
 * 实例化的Disposable需在不用时及时销毁
 */
public abstract class BaseActivity extends AppCompatActivity /*implements BaseView */{
    public CompositeDisposable mCompositeDisposable;
    private Unbinder bind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        bind = ButterKnife.bind(this);

        //组件依赖注入全局级别的Application component
//        setupActivityComponent(mApp.getAppComponent());
        //进行RxBinding 绑定
        mCompositeDisposable = new CompositeDisposable();
        init();
    }

    protected abstract int getLayoutResID();

    protected abstract void init();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearDisposable();
        if (bind != Unbinder.EMPTY) {
            bind.unbind();//解除绑定
        }
    }

    /**
     * 添加订阅
     */
    public void addDisposable(Disposable mDisposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(mDisposable);
    }

    /**
     * 取消所有订阅
     */
    public void clearDisposable() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }


    /**
     * 点击空白区域 自动隐藏软键盘
     *
     * @param  event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            View v = getCurrentFocus();
            //判断是否要收起并进行处理
            if (isShouldHideKeyboard(v, event)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.onTouchEvent(event);
    }

    //判断是否要收起键盘
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // 点击EditText的事件，忽略它。
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上
        return false;
    }

    //隐藏软键盘并让editText失去焦点
    private void hideKeyboard(IBinder token) {
        closFocus();
        if (token != null) {
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    protected void closFocus(){

    }

}
