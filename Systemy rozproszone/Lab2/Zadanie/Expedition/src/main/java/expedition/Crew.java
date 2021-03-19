package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Crew extends AbstractInputUser implements User{
    public static final String CREW_KEY = "crew";
    public static final String EXPEDITION_KEY = "expedition";

    protected Crew(TextColor chatColor) throws IOException, TimeoutException {
        super("<Crew>", chatColor);
        initialize();
    }

    @Override
    public void initialize() throws IOException {
        this.myQueue = "crew-" + this.name;
    }

    @Override
    public void runListenerThreads() throws IOException {
        Consumer consumer = this.adminAndDeliveryConsumer();
        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Admin.ADMIN_KEY);
        this.runQueueThread(myQueue, consumer);
    }

    public Consumer adminAndDeliveryConsumer(){
        final ChatTerminal terminal = this.terminal;
        return new DefaultConsumer(this.channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // TODO
                // if from admin - unpacking 1
                // if from delivery - unpacking 2
                String message = new String(body, StandardCharsets.UTF_8);
                terminal.printBufferForClient("Received: " + message);
            }
        };
    }

    public void exitCheck(String msg, String key) throws IOException {
        if ( AbstractInputUser.EXIT_COMMAND.equals(key) ||
                AbstractInputUser.EXIT_COMMAND.equals(msg) )
            this.close();
    }

    public void activateCrewInput() throws IOException {
        String key = null;
        String msg = null;
        boolean keyFlag = true;
        boolean msgFlag = true;

        //input
        while(true){
            if (keyFlag && msgFlag) {
                terminal.printBufferForClient("Enter required good name: ");
                keyFlag = false;
            }
            if(!keyFlag && msgFlag && key != null){
                terminal.printBufferForClient("Enter additional information: ");
                msgFlag = false;
            }

            terminal.putChar();

            if(key == null && msg == null) {
                key = terminal.getStringToPrint();
                if(key != null)
                    terminal.printBufferForClient(key + "  ");
            }
            if(key != null & msg == null) {
                msg = terminal.getStringToPrint();
                if(msg != null)
                    terminal.printBufferForClient(msg + "  ");
            }
            exitCheck(msg, key);
            if(msg != null) {
                this.sendMsg(msg, key);
                msg = null;
                key = null;
                keyFlag = true;
                msgFlag = true;
            }
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Crew crew = new Crew(new TextColor.RGB(0,255,0));
        crew.runListenerThreads();
        crew.activateCrewInput();
//        // close
//        channel.close();
//        connection.close();
    }
}
