package com.bdxh.mqttconnection;

import android.app.Application;
import android.content.Context;

import com.bdxh.mqttconnection.detach.LoadResourceUtil;
import com.bdxh.mqttconnection.utils.SPUtil;
import com.bdxh.mqttconnection.utils.TheamColorUtil;


import java.util.Calendar;
import androidx.appcompat.app.AppCompatDelegate;
import skin.support.SkinCompatManager;
import skin.support.app.SkinAppCompatViewInflater;
import skin.support.app.SkinCardViewInflater;
import skin.support.constraint.app.SkinConstraintViewInflater;
import skin.support.design.app.SkinMaterialViewInflater;

import static com.bdxh.mqttconnection.utils.SPUtil.getResourcePath;

public class App extends Application {

    public static Context context;
    public static SPUtil mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
//        initTheme();

        SPUtil.init(this);
        LoadResourceUtil.getInstance().init(this ,getResourcePath());
        SkinCompatManager.withoutActivity(this)
//                .addStrategy(new CustomSDCardLoader())          // 自定义加载策略，指定SDCard路径
//                .addStrategy(new ZipSDCardLoader())             // 自定义加载策略，获取zip包中的资源
                .addInflater(new SkinAppCompatViewInflater())     // 基础控件换肤
                .addInflater(new SkinMaterialViewInflater())      // material design
                .addInflater(new SkinConstraintViewInflater())    // ConstraintLayout
                .addInflater(new SkinCardViewInflater())          // CardView v7
//                .setSkinStatusBarColorEnable(true)              // 关闭状态栏换肤
//                .setSkinWindowBackgroundEnable(false)           // 关闭windowBackground换肤
//                .setSkinAllActivityEnable(false)                // true: 默认所有的Activity都换肤; false: 只有实现SkinCompatSupportable接口的Activity换肤
                .loadSkin();

    }

    // 资源分离
    // 系统自带白天黑夜模式 需重启 Activity 局限性较大
    private void initTheme() {
        TheamColorUtil settingUtil = TheamColorUtil.getInstance();
        // 获取是否开启 "自动切换夜间模式"
        if (settingUtil.getIsAutoNightMode()) {
            int nightStartHour = Integer.parseInt(settingUtil.getNightStartHour());
            int nightStartMinute = Integer.parseInt(settingUtil.getNightStartMinute());
            int dayStartHour = Integer.parseInt(settingUtil.getDayStartHour());
            int dayStartMinute = Integer.parseInt(settingUtil.getDayStartMinute());
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            int nightValue = nightStartHour * 60 + nightStartMinute;
            int dayValue = dayStartHour * 60 + dayStartMinute;
            int currentValue = currentHour * 60 + currentMinute;
            if (currentValue >= nightValue || currentValue <= dayValue) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                settingUtil.setIsNightMode(true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                settingUtil.setIsNightMode(false);
            }

        } else {
            // 获取当前主题
            if (settingUtil.getIsNightMode()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    //资源路径
  /*  public static String getResourcePath(){
        return mInstance.getString(Config.SP_RESOURCE_PATH ,"");
    }*/

}
