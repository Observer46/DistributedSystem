package Chat.Utils;

import Chat.ChatChannels.TCPChatChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class ClientData {   // Server side

    private String name;
    private final TCPChatChannel tcpChannel;
    private SocketAddress myUdpAddress;

    public ClientData(String name, TCPChatChannel tcpChannel) {
        this.name = name;
        this.tcpChannel = tcpChannel;
    }

    public SocketChannel getTcpSocketChannel(){
        return this.tcpChannel.getSocketChannel();
    }

    public SocketAddress getUdpAddressServerSide() {
        return this.myUdpAddress;
    }

    public void setUdpAddressForServerSide(SocketAddress address){
        this.myUdpAddress = address;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void tcpSendMsg(String msg) throws IOException {
        this.tcpChannel.sendMsg(msg);
    }

    public String tcpReceiveMsg() throws IOException {
        return this.tcpChannel.receiveMsg();
    }

    public void cleanUp() throws IOException {
        this.tcpChannel.cleanUp();
    }

}
