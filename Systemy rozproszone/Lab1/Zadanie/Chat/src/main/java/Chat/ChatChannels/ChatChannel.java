package Chat.ChatChannels;

import java.io.IOException;

public interface ChatChannel {

    String addressString() throws IOException;
    void cleanUp() throws IOException;

}
