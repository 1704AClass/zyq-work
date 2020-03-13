import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Created by 12699 on 2020/2/14.
 */
public class Consumer02_subscribe_cms {
    //队列名称
    private static final String QUEUE_INFORM_SMS = "inform_queue_sms";
    private static final String EXCHANGE_FANOUT_INFORM="inform_exchange_fanout";
    public static void main(String[] args) {
        try {
            //创建一个与MQ的连接
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");//rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器
            // 创建一个连接
            Connection connection = factory.newConnection();
            //创建与交换机的通道，每个通道代表一个会话
            Channel channel = connection.createChannel();
            //声明交换机 String exchange, BuiltinExchangeType type
            /*** 参数明细
             * * 1、交换机名称
             * * 2、交换机类型，fanout、topic、direct、headers
             * */
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //声明队列
            // channel.queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
            /*** 参数明细：
             * * 1、队列名称
             * * 2、是否持久化
             * 3、是否独占此队
             * * 4、队列不用是否自动删除
             * * 5、参数
             * */
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //交换机和队列绑定String queue, String exchange, String routingKey
            /*** 参数明细
             * * 1、队列名称
             * * 2、交换机名称
             * * 3、路由key *
             */
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_FANOUT_INFORM, "");
            //定义消费方法
            DefaultConsumer consumer = new DefaultConsumer(channel) {
                /***
                 * 消费者接收消息调用此方法
                 * * @param consumerTag 消费者的标签，在channel.basicConsume()去指定
                 * * @param envelope 消息包的内容，可从中获取消息id，消息routingkey，交换机，消息和重传标志 (收到消息失败后是否需要重新发送)
                 * * @param properties
                 * * @param body
                 * * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //路由key
                    String routingKey = envelope.getRoutingKey();
                    //消息id
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String msg = new String(body, "utf-8");
                    System.out.println("receive message.." + msg);
                }
            };
            /*** 监听队列String queue, boolean autoAck,Consumer callback
             * * 参数明细
             * * 1、队列名称
             * * 2、是否自动回复，设置为true为表示消息接收到自动向mq回复接收到了，mq接收到回复会删除消息，设置 为false则需要手动回复 *
             * 3、消费消息的方法，消费者接收到消息后调用此方法
             * */
            channel.basicConsume(QUEUE_INFORM_SMS, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
