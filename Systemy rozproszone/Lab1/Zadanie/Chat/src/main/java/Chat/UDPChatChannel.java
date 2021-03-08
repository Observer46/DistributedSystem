package Chat;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.Selector;

public class UDPChatChannel implements  IChatChannel {

    public static final String UDP_ASCII_ART =
            "\n`     /\\_/\\\n" +
            " /\\  / o o \\\n" +
            "//\\\\ \\~(*)~/\n" +
            "`  \\/   ^ /\n" +
            "   | \\|| ||\n" +
            "   \\ '|| ||\n" +
            "    \\)()-())";
    public static final String UDP_MSG = "-U";
    public static final String UDP_NAME_PREFIX = "-name:";
    public static final int BUFFER_SIZE = 1024;

    private DatagramChannel datagramChannel;
    private ByteBuffer msgBuffer;

    public UDPChatChannel() throws IOException {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.bind(null);
        this.datagramChannel.configureBlocking(false);
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
    }

    public UDPChatChannel(int port) throws IOException {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.bind(new InetSocketAddress("localhost", port));
        this.datagramChannel.configureBlocking(false);
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
    }

    public SocketAddress getAddress() throws IOException {
        InetSocketAddress address = (InetSocketAddress) this.datagramChannel.getLocalAddress();
        return address;
    }

    public Pair<SocketAddress, String> receiveMsg() throws IOException{
        this.msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);
        SocketAddress from =  this.datagramChannel.receive(this.msgBuffer);
        String response = new String(this.msgBuffer.array()).trim();
        return new Pair<>(from, response);
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
}
