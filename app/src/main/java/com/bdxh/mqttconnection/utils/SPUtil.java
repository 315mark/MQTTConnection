package com.bdxh.mqttconnection.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bdxh.mqttconnection.Config;

public class SPUtil {

    private static SharedPreferences sharedPreferences;

    //初始化
    public static void init(Context context){
        sharedPreferences = context.getSharedPreferences(Config.SP_NAME,Context.MODE_PRIVATE);
    }

    //资源路径
    public static String getResourcePath(){
        String resourcePath;
        resourcePath = sharedPreferences.getString(Config.SP_RESOURCE_PATH , "");
        return  resourcePath;
    }

    //资源保存
    public static void put(String key , Object value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value.getClass()  == Boolean.class){
            editor.putBoolean(key,(Boolean) value);
        }

        if (value.getClass() == String.class){
            editor.putString( key, (String) value);
        }

        if (value.getClass() == Integer.class) {
            editor.putInt(key, (Integer) value);
        }

        editor.commit();
    }

    private static void clear(Context context , String... keys){
        for (String key : keys) {
            SharedPreferences settings = context.getSharedPreferences(
                    Config.SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            if (settings.contains(key)) {
                editor.remove(key).commit();
            }
        }
    }


}
