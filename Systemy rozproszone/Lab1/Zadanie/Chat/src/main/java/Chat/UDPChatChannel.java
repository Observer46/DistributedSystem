package Chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class UDPChatChannel implements  IChatChannel {

    public static final String UDP_ASCII_ART = "123";
    public static final String UDP_MSG = "-U";
    public static final String UDP_MULTICAST_MSG = "-M";
    public static final String UDP_NAME_PREFIX = "-name:";
    public static final int BUFFER_SIZE = 1024;

    private DatagramChannel datagramChannel;
    private ByteBuffer msgBuffer;
    private SocketAddress myAddress;

    public UDPChatChannel() throws IOException {
        this.datagramChannel = DatagramChannel.open();
        //this.datagramChannel.bind(new InetSocketAddress(targetAddress, targetPort));
        this.datagramChannel.bind(null);
        this.datagramChannel.configureBlocking(false);
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
    }

    public UDPChatChannel(int port) throws IOException {
        this.datagramChannel = DatagramChannel.open();
        //this.datagramChannel.bind(new InetSocketAddress(targetAddress, targetPort));
        this.datagramChannel.bind(new InetSocketAddress(port));
        this.datagramChannel.configureBlocking(false);
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
    }

    public void setMyAddress(SocketAddress address) { this.myAddress = address; }

    public DatagramChannel getDatagramChannel() { return this.datagramChannel; }

    public SocketAddress getAddress() throws IOException {
        return this.datagramChannel.getRemoteAddress();
    }

    public Pair<SocketAddress, String> receiveMsg() throws IOException{
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
        SocketAddress from =  this.datagramChannel.receive(this.msgBuffer);     // Need that!
        String response = new String(this.msgBuffer.array()).trim();
        return new Pair<SocketAddress, String>(from, response);
    }


    public void sendMsg(String msg, SocketAddress target) throws IOException{
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
        this.msgBuffer = ByteBuffer.wrap(msg.getBytes());
        this.datagramChannel.send(this.msgBuffer, target);
    }

    @Override
    public String addressString() throws IOException{
        return this.datagramChannel.getRemoteAddress().toString();
    }

    @Override
    public void cleanUp()  throws IOException{
        this.datagramChannel.close();
    }

    public SocketAddress getMyAddress() {
        return this.myAddress;
    }
}
