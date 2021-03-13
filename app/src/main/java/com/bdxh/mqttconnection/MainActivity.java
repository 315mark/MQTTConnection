package com.bdxh.mqttconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private MyMqttService.MQBinder binder;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(MainActivity.this, MyMqttService.class)); //开启服务  这种启动不能传递数据
        //服务绑定后，会调用 onServiceConnected
//        Intent bindIntent = new Intent(this, MyMqttService.class);  //这种方式老报错  不知道哪里操作不对
//        bindService(bindIntent,conn,BIND_ABOVE_CLIENT);
    }

    //mqtt 收发消息
    private void sendMsg(String msg) {
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes()); //传输字节
        MyMqttService.publish(message);
    }


    public void sendMsg(View view){
        MyMqttService.MQTT_Publish("发送消息给你");
//        sendMsg("hello world");
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart() {
        super.onStart();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
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


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            binder = (MyMqttService.MQBinder) iBinder;
            binder.getMyService().setCallback(new MyMqttService.Callback() {
                @Override
                public void onDataChange(String data) {
                    Message msg = mHandler.obtainMessage(Config.MQTT_PUSH_SUCCESS_MSG, data);
                    mHandler.sendMessage(msg);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,MyMqttService.class));  // stop 模式不能传递数据
//        unbindService(conn);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (MyMqttService.MQBinder) iBinder;
        binder.getMyService().setCallback(new MyMqttService.Callback() {
            @Override
            public void onDataChange(String data) {
                Message msg = mHandler.obtainMessage(Config.MQTT_PUSH_SUCCESS_MSG, data);
                mHandler.sendMessage(msg);
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
