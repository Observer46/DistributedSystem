package Chat.ChatChannels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPChatChannel implements ChatChannel {

    private final SocketChannel socketChannel;
    private ByteBuffer msgBuffer;

    // Server side
    public TCPChatChannel(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        this.socketChannel = serverSocket.accept();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        this.msgBuffer = ByteBuffer.allocate(512);
    }

    // Client side
    public TCPChatChannel(InetAddress serverAddress, int serverPort) throws IOException {
        InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress, serverPort);
        this.socketChannel = SocketChannel.open(serverSocketAddress);
        this.msgBuffer = ByteBuffer.allocate(512);
    }

    public void setNonBlocking() throws IOException {
        this.socketChannel.configureBlocking(false);
    }

    public SocketChannel getSocketChannel(){
        return this.socketChannel;
    }

    public String receiveMsg() throws IOException {
        this.msgBuffer = ByteBuffer.allocate(512);
        this.socketChannel.read(this.msgBuffer);
        String response = new String(this.msgBuffer.array()).trim();
        return response.length() == 0 ? null : response;
    }

    public void sendMsg(String msg) throws IOException {
        this.msgBuffer = ByteBuffer.allocate(512);
        this.msgBuffer = ByteBuffer.wrap(msg.getBytes());
        this.socketChannel.write(this.msgBuffer);
    }

    @Override
    public void cleanUp() throws IOException {
        this.socketChannel.close();
    }

    @Override
    public String addressString() throws IOException {
        return this.socketChannel.getRemoteAddress().toString();
    }
}
