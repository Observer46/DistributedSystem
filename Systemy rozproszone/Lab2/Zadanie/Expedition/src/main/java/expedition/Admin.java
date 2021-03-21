package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Admin extends AbstractInputUser implements User{

    public static final String BROADCAST_KEY = "expedition";
    public static final String CREW_OPTION = "-c ";
    public static final String DELIVERY_OPTION = "-d ";
    public static final String ADMIN_KEY = "*.*";
    public static final String ADMIN_PREFIX = "admin";

    protected Admin(TextColor chatColor) throws IOException, TimeoutException {
        super("<Admin>", chatColor);
        initialize();
    }

    @Override
    public void initialize() {
        this.myQueue = Admin.ADMIN_PREFIX + "-" + this.name;
    }

    @Override
    public void runListenerThreads() throws IOException {
        Consumer adminConsumer = this.defaultConsumer();
        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Admin.ADMIN_KEY);
        this.runQueueThread(myQueue, adminConsumer);
    }

    @Override
    public String prepareMsg(String rawMsg) {
        return Admin.ADMIN_PREFIX + "&" + this.name + "&" + rawMsg;
    }

    public void processMsg(String msg) throws IOException {
        if(msg.startsWith(AbstractInputUser.EXIT_COMMAND))
            this.close();

        String key = Admin.BROADCAST_KEY;
        msg = msg.trim();
        if(msg.startsWith("-")){
            if(msg.startsWith(Admin.CREW_OPTION)) {
                key = Crew.CREW_KEY;
                msg = msg.substring(3).trim();
            }
            else if (msg.startsWith(Admin.DELIVERY_OPTION)) {
                key = Delivery.DELIVERY_KEY;
                msg = msg.substring(3).trim();
            }
            else {
                this.terminal.printBufferForClient("Unknown option - message not sent");
                return;
            }
        }
        terminal.printBufferForClient(msg + "  ");
        this.sendMsg(prepareMsg(msg), key);
    }

    public void activateAsyncInput() throws IOException, InterruptedException {
        while(true){
            terminal.putChar();
            String msg = terminal.getStringToPrint();
            if(msg != null) {
                processMsg(msg);
            }
            Thread.sleep(10);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Admin admin = new Admin(new TextColor.RGB(0, 255, 0));
        admin.runListenerThreads();
        admin.activateAsyncInput();
    }
}
