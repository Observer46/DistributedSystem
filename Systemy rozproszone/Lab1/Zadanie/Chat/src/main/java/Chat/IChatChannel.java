package Chat;

import java.io.IOException;

public interface IChatChannel {

    //String receiveMsg() throws IOException;
    //void sendMsg(String msg) throws IOException;

    String addressString() throws IOException;
    void cleanUp() throws IOException;
}
