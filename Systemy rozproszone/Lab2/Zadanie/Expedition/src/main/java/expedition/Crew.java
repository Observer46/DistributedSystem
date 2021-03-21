package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Crew extends AbstractInputUser implements User{

    public static final String CREW_KEY = "crew";
    public static final String CREW_PREFIX = CREW_KEY;

    protected Crew(TextColor chatColor) throws IOException, TimeoutException {
        super("<Crew>", chatColor);
        initialize();
    }

    @Override
    public void initialize() {
        this.myQueue = Crew.CREW_PREFIX + "-" + this.name;
    }

    @Override
    public void runListenerThreads() throws IOException {
        Consumer consumer = this.defaultConsumer();
        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Admin.BROADCAST_KEY);
        this.bindQueue(myQueue, Crew.CREW_KEY);
        this.bindQueue(myQueue,  Crew.CREW_PREFIX + "." + this.name);
        this.runQueueThread(myQueue, consumer);
    }

    @Override
    public String prepareMsg(String rawMsg) {
        return Crew.CREW_KEY + "&" + this.name + "&" + rawMsg;
    }

    public void exitCheck(String msg, String key) throws IOException {
        if ( AbstractInputUser.EXIT_COMMAND.equals(key) ||
                AbstractInputUser.EXIT_COMMAND.equals(msg) )
            this.close();
    }

    public void activateCrewInput() throws IOException, InterruptedException {
        String key = null;
        String msg = null;
        boolean keyFlag = true;
        boolean msgFlag = true;

        //input
        while(true){
            if (keyFlag) {
                terminal.printBufferForClient("Enter required good name: ");
                keyFlag = false;
            }
            if(msgFlag && key != null){
                terminal.printBufferForClient("Enter additional information: ");
                msgFlag = false;
            }

            terminal.putChar();

            if(key == null) {
                key = terminal.getStringToPrint();
                if(key != null) {
                    key = key.trim();
                    terminal.printBufferForClient(key + "  ");
                }
            }
            if(key != null) {
                msg = terminal.getStringToPrint();
                if(msg != null) {
                    msg = msg.trim();
                    terminal.printBufferForClient(msg + "  ");
                }
            }
            exitCheck(msg, key);
            if(msg != null) {
                String parsedKey = Delivery.EQ_PREFIX + "." + key;
                this.sendMsg(prepareMsg(msg), parsedKey);
                msg = null;
                key = null;
                keyFlag = true;
                msgFlag = true;
            }

            Thread.sleep(10);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Crew crew = new Crew(new TextColor.RGB(255,255,255));
        crew.runListenerThreads();
        crew.activateCrewInput();
    }
}
