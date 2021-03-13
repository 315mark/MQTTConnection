package com.bdxh.mqttconnection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import androidx.annotation.Nullable;

public class MyMqttService extends Service {
    public final String TAG = MyMqttService.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    private static final String clientid ="";

    private Callback callback;
    private String data = "服务器正在执行";

//    设备唯一标识也可  收发
//    DeviceIdUtil.getDeviceId(context)

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        //界面通过Intent将数据传递过来
        intent.getStringExtra("data");
        return super.onStartCommand(intent, flags, startId);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MQBinder();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class MQBinder extends Binder {

        public void setData(String data){
            MyMqttService.this.data = data;
        }

        public MyMqttService getMyService(){
            return MyMqttService.this;
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
//        init();
    }

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息  retain
     */
    public static void publish(String message) {
        String topic = Config.RESPONSE_TOPIC;
        Integer qos = 1;
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained);
            IMqttDeliveryToken token = mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));
            token.waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // 发布消息
    public static void MQTT_Publish(String message) {
        String topic = Config.RESPONSE_TOPIC;
        Integer qos = 1;
        Boolean retained = false;// 是否在服务器保留断开连接后的最后一条消息
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos, retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     */
    public static void publish(MqttMessage message) {
        String topic = Config.PUBLISH_TOPIC;
        try {
            mqttAndroidClient.publish(topic,message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    public void response(String message) {
        String topic = Config.RESPONSE_TOPIC;
        Integer qos = 1;
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        String serverURI = Config.HOST; //服务器地址（协议+地址+端口号）
        try {
            mqttAndroidClient = new MqttAndroidClient(this, serverURI, clientid);
            mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
            mMqttConnectOptions = new MqttConnectOptions();
            mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存  否则会重复提交
            mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
            mMqttConnectOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
            mMqttConnectOptions.setUserName(Config.USERNAME); //设置用户名
            mMqttConnectOptions.setPassword(Config.PASSWORD.toCharArray()); //设置密码

            MemoryPersistence persistence = new MemoryPersistence();
//        if (mqttAndroidClient.isConnected()) {
//            mqttAndroidClient.close();
//            mqttAndroidClient.disconnect();
//        }


            // last will message
            boolean doConnect = true;
            String message = "{\"terminal_uid\":\"" + "101010100100"+ "\"}";
            String topic = Config.PUBLISH_TOPIC;
            Integer qos = 2;
            Boolean retained = false;
            // 最后的遗嘱
            try {
                mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained);
            } catch (Exception e) {
                LogUtils.e( "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
            if (doConnect) {
                doClientConnection();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!mqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            return true;
        } else {
            LogUtils.i("没有可用网络");
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                     doClientConnection();
                }
            },3000);
            return false;
        }
    }

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            ToastUtils.showLong("连接成功 ");
            try {
                mqttAndroidClient.subscribe( Config.PUBLISH_TOPIC, 1);//订阅主题，参数：主题、服务质量
                if (callback !=null){
                    callback.onDataChange("连接成功");
                }
            } catch (MqttException e) {
                e.printStackTrace();

            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            LogUtils.e("连接失败 ");
//            doClientConnection();//连接失败，重连（可关闭服务器进行模拟）
        }
    };


    //订阅主题的回调
    private MqttCallbackExtended mqttCallback = new MqttCallbackExtended() {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            //连接成功的回掉
            ToastUtils.showLong("MQTT连接成功");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LogUtils.i("收到消息： " + new String(message.getPayload()));
            //收到消息，这里弹出Toast表示。如果需要更新UI，可以使用广播或者EventBus进行发送
            ToastUtils.showLong( "messageArrived: " + new String(message.getPayload()));
            //TODO  收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等 收到消息给个回馈
            response("收到信息回馈");
            if (callback != null){
                callback.onDataChange(new String(message.getPayload()));
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            //publish后会执行到这
            LogUtils.i( "消息成功发送");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            LogUtils.i( "连接断开 ");
            doClientConnection();//连接断开，重连
        }
    };

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {

        void onDataChange(String msg);
    }

}
