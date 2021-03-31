package com.bdxh.mqttconnection;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.SkinAppCompatDelegateImpl;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import skin.support.SkinCompatManager;
import skin.support.app.SkinCompatActivity;
import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

/**
 * 基础Activity   封装RxBinding
 * 实例化的Disposable需在不用时及时销毁
 */
public abstract class BaseActivity extends AppCompatActivity implements SkinCompatSupportable /* BaseView */{
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

    //换肤重写方法
    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return SkinAppCompatDelegateImpl.get(this, this);
    }

    @Override
    public void applySkin() {
//        updateStatusBarColor();
    }


    private void updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(SkinCompatResources.getColor(this, R.color.colorPrimary));
        }
        // 修改状态栏字体颜色
        boolean useDarkStatusBar = getResources().getBoolean(R.bool.use_dark_status);
//        int resId = SkinCompatResources.getInstance().getTargetResId(this, R.bool.use_dark_status);
//
//        if (resId != 0) {
//            useDarkStatusBar = SkinCompatResources.getInstance().getSkinResources().getBoolean(resId);
//        }
//        if (useDarkStatusBar) {
//            SkinStatusBarUtils.setStatusBarDarkMode(this);
//        } else {
//            SkinStatusBarUtils.setStatusBarLightMode(this);
//        }
    }


    //切换方法  该方法通过配置一套 value-night colors进行换肤  后缀加载
    protected void openSkinApp(){
        SkinCompatManager.getInstance().loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
    }

    //取消的方法
    protected void closeSkin(){
        SkinCompatManager.getInstance().restoreDefaultTheme();
    }

//    特殊需求  设置某些控件不跟随换肤改变颜色                   // 这种方式设置依旧会改变
//    setBackgroundDrawable(redDrawable)                      //  setBackgroundResource(R.drawable.red)
//    background="#ce3d3a"    布局操作                        //  background="@drawable/red"



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
