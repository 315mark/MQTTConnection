package com.bdxh.mqttconnection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bdxh.mqttconnection.detach.LoadResourceUtil;
import com.blankj.utilcode.util.LogUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;

public class MainActivity extends BaseActivity /*implements ServiceConnection*/ {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    //    private MyMqttService.MQBinder binder;
    private Handler mHandler;

    @BindView(R.id.loadResourceImg)
    TextView tvImg;
    @BindView(R.id.loadResourceTxt)
    TextView txt;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
//        startService(new Intent(MainActivity.this, MyMqttService.class)); //开启服务  这种启动不能传递数据
        //服务绑定后，会调用 onServiceConnected
//        Intent bindIntent = new Intent(this, MyMqttService.class);
//        bindService(bindIntent,conn,BIND_ABOVE_CLIENT);
        LogUtils.d("onCreate");
        checkPermissions();

        txt.setOnClickListener(view -> {
            // 加载资源apk文件中的文字
            txt.setText(LoadResourceUtil.getInstance().getString("test_text"));
            // 加载资源apk文件中的颜色
            txt.setTextColor(LoadResourceUtil.getInstance().getColor("test_color"));
        });

        tvImg.setOnClickListener(view -> {
            Drawable changeConfig = LoadResourceUtil.getInstance().getDrawable("test_img");
            if (changeConfig != null) {
                tvImg.setBackground(changeConfig);
            }
        });
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

//        stopService(new Intent(this,MyMqttService.class));  // stop 模式不能传递数据
        LogUtils.d("结束页面");
//        unbindService(conn);
//        MyMqttService.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                java.util.Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "允许存储权限", Toast.LENGTH_SHORT).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 申请权限
     */
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }else{// else: We already have permissions, so handle as normal
        }
    }

    public void loadResource(View view) {
        // getExternalFilesDir 调用这个 api
        // 加载sd卡路径下的资源apk文件  Android 10版本路径有坑 需要将apk 数据放在 /storage/emulated/0/Android/data/<包名>/files
        LoadResourceUtil.getInstance().setLoadResource(Environment.getExternalStorageDirectory() + "/testResource1.apk");
    }

}
