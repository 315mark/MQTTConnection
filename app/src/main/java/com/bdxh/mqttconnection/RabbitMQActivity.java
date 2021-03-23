package com.bdxh.mqttconnection;

import android.os.Bundle;
import android.widget.TextView;
import com.blankj.utilcode.util.LogUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RabbitMQActivity extends AppCompatActivity {

    TextView rabbitMq;
    TextView rabbitMqSend;

    private String userName = "admin";
    private String passWord = "admin123";
    private String virtualHost = "/";
    private String hostName = "192.168.0.61";
    private int portNum =5672;

    String queueName2 /*= "amp.fanout"*/;
    private String queueName3 = "test002";
    private String exchangeName = "sendMsg";
    private String rountingKey = "testconn";

    ConnectionFactory factory  ;
    Connection sendConnection , receiveConnection;
    Channel sendChannel ,receiveChannel;
    String queueName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rabbitmq);
        rabbitMq = findViewById(R.id.rabbit_mq);
        rabbitMqSend = findViewById(R.id.rabbit_mq_send);
        init();
    }


    protected void init() {
        setupConnectionFactory();
        ThreadManager.getThreadPool().execute(() -> {
            try {
                //发送连接
                sendConnection = factory.newConnection();
                //发送通道
                sendChannel = sendConnection.createChannel();
                //声明了一个交换和一个服务器命名的队列，然后将它们绑定在一起。
                sendChannel.exchangeDeclare(rountingKey, "fanout", true);

                //收连接
                receiveConnection = factory.newConnection();
                //收通道
                receiveChannel = receiveConnection.createChannel();
                queueName = sendChannel.queueDeclare().getQueue();
                queueName2 = receiveChannel.queueDeclare().getQueue();
                LogUtils.d(" 消息队列 " + queueName );

            } catch (IOException | TimeoutException ex) {
                ex.printStackTrace();
            }
        });

        rabbitMqSend.setOnClickListener(view -> ThreadManager.getThreadPool().execute(this::basicPublish));

        rabbitMq.setOnClickListener(view -> ThreadManager.getThreadPool().execute(this::basicConsume));

    }

    /**
     * Rabbit配置
     */
    private void setupConnectionFactory() {

        factory = new ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(passWord);
        factory.setHost(hostName);
        factory.setPort(portNum);
        factory.setAutomaticRecoveryEnabled(true);//设置网络异常重连
        factory.setRequestedHeartbeat(1);//是否断网
    }

    /**
     * 发消息   这种情况要使用交换机绑定
     */
    private void basicPublish() {
        try {
            String queueNa = sendChannel.queueDeclare().getQueue();
//            String queueName = channel.queueDeclare().getQueue();  这是随机生产的队列名  rountingKey  路由键
            LogUtils.d(" 消息队列 " + queueName2);
            sendChannel.queueBind(queueName2, exchangeName, "");
            //一次只发送一个，处理完成一个再获取下一个
            sendChannel.basicQos(1);
            //消息发布
            byte[] msg = "hello word!".getBytes();
            sendChannel.basicPublish(exchangeName , "" , null, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 收消息
     */
    private void basicConsume() {
        try {
            //消费者声明自己的队列
//            receiveChannel.queueDeclare(queueName2, false, false, false, null);
//            LogUtils.d(" queueName  " + queueName2);
            // 声明exchange，指定类型为direct
//            receiveChannel.exchangeDeclare(rountingKey, "topic");
            //消费者将队列与交换机进行绑定
            receiveChannel.queueBind(queueName2, exchangeName, "");
            LogUtils.d(" queueName  " + queueName2);
            //实现Consumer的最简单方法是将便捷类DefaultConsumer子类化。可以在basicConsume 调用上传递此子类的对象以设置订阅 ：
            receiveChannel.basicConsume(queueName3, false, "administrator", new DefaultConsumer(receiveChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);

                    String rountingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    String msg = new String(body);
                    long deliveryTag = envelope.getDeliveryTag();

                    LogUtils.d(rountingKey + "：rountingKey");
                    LogUtils.d(contentType + "：contentType");
                    LogUtils.d(msg + "：msg");
                    LogUtils.d(deliveryTag + "：deliveryTag");

//                    channel.basicAck(deliveryTag, false);

                    rabbitMqSend.setText(msg);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
