package Chat;


import java.io.*;
import java.net.*;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client {

    public static final String END_MSG = "-exit";
    public static final String NO_NAME = "unnamed";

    private String name;
    private TCPChatChannel tcpChannel;
    private UDPChatChannel udpChannel;
    private UDPChatChannel multicastChannel;    // maybe not needed
    private BufferedReader stdIn;

    public Client(String name, TCPChatChannel tcpChannel) throws IOException {
        this.name = name;
        this.tcpChannel = tcpChannel;
    }

    public Client(InetAddress serverAddress, int serverPort) throws IOException {
        this.tcpChannel = new TCPChatChannel(serverAddress, serverPort);
        this.stdIn = new BufferedReader(new InputStreamReader(System.in));
        this.name = Client.NO_NAME;

        while(this.name.equals(Client.NO_NAME)){
            System.out.println(this.tcpChannel.receiveMsg());
            String name = this.stdIn.readLine();
            this.tcpChannel.sendMsg(name);
            String response = this.tcpReceiveMsg();

            if(response.equals(Server.ACCEPT_NAME))
                this.name = name;
            this.tcpChannel.setNonBlocking();
        }
    }

    public SocketChannel getTcpSocketChannel(){
        return this.tcpChannel.getSocketChannel();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void tcpSendMsg(String msg) throws IOException {
        this.tcpChannel.sendMsg(msg);
        if(msg.equals(Client.END_MSG))
            this.cleanUp();
    }

    public String tcpReceiveMsg() throws IOException {
        return this.tcpChannel.receiveMsg();
    }

    public void udpSendMsg(String msg, SocketAddress target) throws IOException {
        this.udpChannel.sendMsg(msg, target);
    }

    public String udpReceiveMsg() throws IOException {
        return this.udpChannel.receiveMsg();
    }

    public void cleanUp() throws IOException {
        this.tcpChannel.cleanUp();
        this.udpChannel.cleanUp();
    }

    public String tcpAddressString() throws IOException {
        return this.tcpChannel.addressString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int port = Server.SERVER_PORT;

        Client client = new Client(serverAddress, port);
        while(true){
            String msg = client.tcpReceiveMsg();
            if(msg != null)
                System.out.println(msg);
            client.tcpSendMsg(client.stdIn.readLine());
        }
    }
}
