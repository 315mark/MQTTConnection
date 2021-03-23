package com.bdxh.mqttconnection;

import android.Manifest;

public class Config {

    //svn://120.24.246.112/bdxh/project/SmartCampus/client/VoiceDemo


    public static final String PARAM = "param";
    public static final String TOKEN = "token";
    public static final String BLEDEVICE = "bledevice";
    public static final String isLOGIN = "isLogin";
    public static final String isPOLLCONTROL = "isPollControl";
    public static final String CONN_BLE_MAC="connBleMac";
    public static final String CONN_TIME="connTime";
    public static final String isPOLL = "isPoll";
    public static final String Config_Title = "config_title";
    public static final String Config_Show_Icon = "showIcon";



    public static final String KEY_DATA = "key_data";
    public static final String[] mPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION ,
            Manifest.permission.ACCESS_COARSE_LOCATION ,
            Manifest.permission.WRITE_EXTERNAL_STORAGE ,
            Manifest.permission.READ_PHONE_STATE
    };

    public static final int REQUEST_CODE_OPEN_GPS = 1;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final int REQUEST_CODE_LOCATION = 887;
    public static final int REQUEST_CODE_BLUETOOTH = 886;
    public static final int REQUEST_READ_PHONE_STATE = 885;
    public static final int RUSH_DATA_INFO = 233;

    public static String HOST = "tcp://192.168.0.66:1883";//服务器地址（协议+地址+端口号）
    public static String USERNAME = "admin";//用户名
    public static String PASSWORD = "admin";//密码
    public static String PUBLISH_TOPIC = "demo/src/repson";//发布主题
    public static String RESPONSE_TOPIC = "demo/src";//响应主题
    public static String SUBSCRIBE_TOPIC = "demo/src/repson";//订阅主题

//    //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示
//    @SuppressLint("MissingPermission")
//    public static String CLIENTID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Build.getSerial() : Build.SERIAL;


    public static final int MQTT_PUSH_SUCCESS_MSG = 18;

    public static final int RECONNECT = 122;

}
