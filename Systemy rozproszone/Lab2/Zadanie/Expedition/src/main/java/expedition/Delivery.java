package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Delivery extends AbstractUser implements User {

    public static final String DELIVERY_KEY = "delivery";
    public static final String EQ_PREFIX = "eq";
    public static final String DELIVERY_PREFIX = DELIVERY_KEY;

    private final ArrayList<String> equipmentList = new ArrayList<>();

    public Delivery(TextColor chatColor) throws IOException, TimeoutException {
        super("<Delivery>", chatColor);
        initialize();
    }

    @Override
    public void initialize() throws IOException {
        this.myQueue = Delivery.DELIVERY_PREFIX + "-" + this.name;
        this.terminal.printBufferForClient( "Enter offered equipment: ");
        String goodsString = this.terminal.getInputBlocking();
        String[] goods = goodsString.split(" ");
        this.terminal.eraseCursor();

        Collections.addAll(this.equipmentList, goods);
    }

    @Override
    public void runListenerThreads() throws IOException {
        Consumer adminConsumer = this.defaultConsumer();
        Consumer equipmentConsumer = this.equipmentConsumer();

        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Delivery.DELIVERY_KEY);
        this.bindQueue(myQueue, Admin.BROADCAST_KEY);
        this.runQueueThread(myQueue, adminConsumer);

        for(final String eq : this.equipmentList) {
            String eqKey = Delivery.EQ_PREFIX + "." + eq;
            this.declareQueue(eq);
            this.bindQueue(eq, eqKey);
            this.runEquipmentThread(eq, equipmentConsumer);
        }
    }

    public void runEquipmentThread(final String queueName, final Consumer consumer){
        Thread equipmentListener = new Thread(){
            @Override
            public void run() {
                try {
                    channel.basicConsume(queueName, false, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        equipmentListener.start();
    }

    @Override
    public String prepareMsg(String rawMsg) {
        return Delivery.DELIVERY_PREFIX + "&" + this.name + "&" + rawMsg;
    }

    public Consumer equipmentConsumer(){
        final Channel channel = this.channel;
        final ChatTerminal terminal = this.terminal;
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                String[] msgContent = message.split("&");
                if(msgContent.length != 3){
                    terminal.printBufferForClient("Received unknown msg: " + message);
                    return;
                }

                String role = msgContent[0];
                String name = msgContent[1];
                String msg  = msgContent[2];
                if(!role.equals(Crew.CREW_PREFIX)) {
                    terminal.printBufferForClient("Received order from unknown source - not a crew!");
                    terminal.printBufferForClient("Details:  Role: " + role + ", Name: " + name + ", message: " + msg);
                    return;
                }

                String equipment = envelope.getRoutingKey().split("\\.")[1];
                UUID orderId = UUID.randomUUID();
                String orderString = orderId.toString();
                orderString = orderString.substring(orderString.length() * 3 / 4);
                terminal.printBufferForClient("Received order " + orderString + " for: " + equipment + " from crew: " + name);
                terminal.printBufferForClient("Additional info: " + msg);

                String reply = "Order " + orderString + " for " + equipment + " successfully realised";
                String crewKey = Crew.CREW_PREFIX + "." + name;
                sendMsg(prepareMsg(reply), crewKey);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        final Delivery delivery = new Delivery(new TextColor.RGB(255, 255, 0));
        delivery.runListenerThreads();
    }
}
