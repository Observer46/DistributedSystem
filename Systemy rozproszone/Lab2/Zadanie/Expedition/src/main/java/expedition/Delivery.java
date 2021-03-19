package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class Delivery extends AbstractUser implements User {

    public static final String DELIVERY_KEY = "delivery";

    private final ArrayList<String> goods = new ArrayList<>();

    public Delivery(TextColor chatColor) throws IOException, TimeoutException {
        super("<Delivery>", chatColor);
        initialize();
    }

    public void initialize() throws IOException {
        this.myQueue = "delivery-" + this.name;
        this.terminal.printBufferForClient( "Enter offered goods: ");
        String goodsString = this.terminal.getInputBlocking();
        String[] goods = goodsString.split(" ");
        this.terminal.eraseCursor();

        Collections.addAll(this.goods, goods);
    }

    public Consumer equipmentConsumer(){
        final Channel channel = this.channel;
        final ChatTerminal terminal = this.terminal;
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                terminal.printBufferForClient("Received: " + message);
                terminal.printBufferForClient("Done with message: " + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
    }

    public void runListenerThreads() throws IOException {
        Consumer adminConsumer = this.adminConsumer();
        Consumer equipmentConsumer = this.equipmentConsumer();

        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Delivery.DELIVERY_KEY);
        this.bindQueue(myQueue, Admin.BROADCAST_KEY);
        this.runQueueThread(myQueue, adminConsumer);

        for(final String good : this.goods) {
            this.declareQueue(good);
            this.bindQueue(good, good);
            this.runQueueThread(good, equipmentConsumer);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        final Delivery delivery = new Delivery(new TextColor.RGB(0, 0, 255));
        delivery.runListenerThreads();
    }
}
