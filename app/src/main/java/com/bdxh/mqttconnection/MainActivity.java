package com.bdxh.mqttconnection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends BaseActivity /*implements ServiceConnection*/ {

//    private MyMqttService.MQBinder binder;
    private Handler mHandler;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        startService(new Intent(MainActivity.this, MyMqttService.class)); //开启服务  这种启动不能传递数据
        //服务绑定后，会调用 onServiceConnected
//        Intent bindIntent = new Intent(this, MyMqttService.class);
//        bindService(bindIntent,conn,BIND_ABOVE_CLIENT);
        LogUtils.d("onCreate");
    }

    //mqtt 收发消息
    private void sendMsg(String msg) {
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes()); //传输字节
        MyMqttService.publish(message);
    }

    public void changeSkin(View view) {
        openSkinApp();
    }

    public void closeSkin(View view) {
        closeSkin();
    }

    public void sendMsg(View view){
//        MyMqttService.MQTT_Publish("发送消息给你");

      /*    // 这种重启 Activity 换肤弊端太多  一键换肤更方便
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            TheamColorUtil.getInstance().setIsNightMode(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            TheamColorUtil.getInstance().setIsNightMode(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
        recreate();
        */
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart() {
        super.onStart();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case Config.MQTT_PUSH_SUCCESS_MSG:
                        String data = (String) msg.obj;
                        LogUtils.d(data);
                        break;
                    case CONTEXT_IGNORE_SECURITY:

                        break;

                    default:
                        break;
                }
                //在此处更新UI
                super.handleMessage(msg);
            }
        };
    }


/*
        //  binder 绑定页面才会用这个
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            binder = (MyMqttService.MQBinder) iBinder;
            binder.getMyService().setCallback(data -> {
                Message msg = mHandler.obtainMessage(Config.MQTT_PUSH_SUCCESS_MSG, data);
                mHandler.sendMessage(msg);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };*/




    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this,MyMqttService.class));  // stop 模式不能传递数据
        LogUtils.d("结束页面");
//        unbindService(conn);
        MyMqttService.destroy();
    }

}
