package Chat.ChatChannels;

import Chat.Utils.Pair;

import java.io.IOException;
import java.net.SocketAddress;

public interface UDPChannel extends ChatChannel {
    Pair<SocketAddress, String> receiveMsg() throws IOException;
}
