package expedition;

import com.googlecode.lanterna.TextColor;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Admin extends AbstractInputUser implements User{

    public static final String BROADCAST_KEY = "expedition";
    public static final String CREW_OPTION = "-c";
    public static final String DELIVERY_OPTION = "-d";
    public static final String ADMIN_KEY = "*.*";

    protected Admin(TextColor chatColor) throws IOException, TimeoutException {
        super("<Admin>", chatColor);
        initialize();
    }

    @Override
    public void initialize() throws IOException {
        this.myQueue = "admin-" + this.name;
    }

    @Override
    public void runListenerThreads() throws IOException {
        Consumer adminConsumer = this.adminConsumer();
        this.declareQueue(myQueue);
        this.bindQueue(myQueue, Admin.ADMIN_KEY);
        this.runQueueThread(myQueue, adminConsumer);
    }

    public void processMsg(String msg) throws IOException {
        if(msg.startsWith(AbstractInputUser.EXIT_COMMAND))
            this.close();

        String key = Admin.BROADCAST_KEY;
        if(msg.startsWith("-")){
            if(msg.startsWith(Admin.CREW_OPTION))
                key = Crew.CREW_KEY;
            else if (msg.startsWith(Admin.DELIVERY_OPTION))
                key = Delivery.DELIVERY_KEY;
            else {
                this.terminal.printBufferForClient("Unknown option - message not sent");
                return;
            }
        }
        String message = ": " + msg.substring(key.length() + 1).trim();
        if(!key.equals(Admin.BROADCAST_KEY))
            message = "(" + (key.equals(Crew.CREW_KEY) ?  CREW_OPTION : DELIVERY_OPTION ) + ")" + message;
        message = "Admin " + this.name + message;
       this.sendMsg(message, key);
    }

    public void activateAsyncInput() throws IOException {
        while(true){
            terminal.putChar();
            String msg = terminal.getStringToPrint();
            if(msg != null) {
                processMsg(msg);
            }
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Admin admin = new Admin(new TextColor.RGB(255, 0, 0));
        admin.runListenerThreads();
        admin.activateAsyncInput();
    }
}
