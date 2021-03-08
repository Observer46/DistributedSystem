package Chat;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;

public class MulticastChatChannel  implements IChatChannel{

    public static final String MULTICAST_MSG = "-M";
    public static final int BUFFER_SIZE = 1024;
    public static final String MULTICAST_ADDRESS = "225.255.0.1";
    public static final int MULTICAST_PORT = 1337;

    private DatagramChannel multicastChannel;
    private ByteBuffer msgBuffer;
    private MembershipKey multicastKey;

    public MulticastChatChannel() throws IOException {
        InetAddress multicastAddress = InetAddress.getByName(MulticastChatChannel.MULTICAST_ADDRESS);
        NetworkInterface netInt = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

        this.multicastChannel = DatagramChannel.open(StandardProtocolFamily.INET);
        this.multicastChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.multicastChannel.bind(new InetSocketAddress(MULTICAST_PORT));
        this.multicastChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, netInt);
        this.multicastChannel.configureBlocking(false);

        this.msgBuffer = ByteBuffer.allocate(MulticastChatChannel.BUFFER_SIZE);
        this.multicastKey = this.multicastChannel.join(multicastAddress, netInt);
    }

    public DatagramChannel getMulticastChannel() { return this.multicastChannel; }

    public Pair<SocketAddress, String> receiveMsg() throws IOException{     // Only passive side
        if(this.multicastKey.isValid()) {
            this.msgBuffer = ByteBuffer.allocate(MulticastChatChannel.BUFFER_SIZE);
            SocketAddress from = this.multicastChannel.receive(this.msgBuffer);
            String response = new String(this.msgBuffer.array()).trim();
            return new Pair<SocketAddress, String>(from, response);
        }
        return null;    // oof
    }

    @Override
    public String addressString() throws IOException{
        return this.multicastChannel.getRemoteAddress().toString();
    }

    @Override
    public void cleanUp()  throws IOException{
        this.multicastChannel.close();
    }
}
