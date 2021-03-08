package Chat;


import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;

public class Client {

    public static final String END_MSG = "-exit";
    public static final String NO_NAME = "unnamed";

    private String name;
    private TCPChatChannel tcpChannel;
    private UDPChatChannel udpChannel;
    private MulticastChatChannel multicastChannel;    // maybe not needed
    private BufferedReader stdIn;
    private SocketAddress udpServerSocketAddress;
    private SocketAddress myUdpAddress;

    public Client(String name, TCPChatChannel tcpChannel) throws IOException {
        this.name = name;
        this.tcpChannel = tcpChannel;
    }

    public Client(InetAddress serverAddress, int serverPort) throws IOException {
        this.udpChannel = new UDPChatChannel();
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
        }

        this.tcpChannel.setNonBlocking();   // ??? was in 'while'
        this.udpServerSocketAddress = new InetSocketAddress("localhost", Server.SERVER_PORT);
        String udpValidation = UDPChatChannel.UDP_NAME_PREFIX + this.name;
        this.udpSendMsg(udpValidation, this.udpServerSocketAddress);    // needs confirmation
        this.multicastChannel = new MulticastChatChannel();
    }

    public SocketChannel getTcpSocketChannel(){
        return this.tcpChannel.getSocketChannel();
    }

    public SocketAddress getUdpAddress() throws IOException {
        return this.udpChannel.getAddress();
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
        if(msg.equals(Client.END_MSG))
            this.cleanUp();
    }

    public String tcpReceiveMsg() throws IOException {
        return this.tcpChannel.receiveMsg();
    }

    public void udpSendMsg(String msg, SocketAddress target) throws IOException {
        this.udpChannel.sendMsg(msg, target);
    }

    public String multicastReceiveMsg() throws IOException {
        Pair<SocketAddress, String> datagramInfo =  this.multicastChannel.receiveMsg();
        if(datagramInfo != null){
            SocketAddress from = datagramInfo.getFirst();
            if(from.equals(this.getUdpAddress()))
                return null;
            String msg = datagramInfo.getSecond();
            return msg;
        }
        return "";
    }

    public Pair<SocketAddress, String> udpReceiveMsg() throws IOException {
        return this.udpChannel.receiveMsg();
    }

    public void cleanUp() throws IOException {
        this.tcpChannel.cleanUp();
        this.udpChannel.cleanUp();
    }

    public String tcpAddressString() throws IOException {
        return this.tcpChannel.addressString();
    }

    public String udpAddressString() throws IOException {
        return this.udpChannel.addressString();
    }

    public void parseAndSendMsg(String msg) throws IOException {
//        if(msg.length() == 0)
//            return;

        if(msg.startsWith(UDPChatChannel.UDP_MSG)){
            String parsedMsg = msg.substring(2);
            if(parsedMsg.length() > 1)
                this.udpSendMsg(parsedMsg, this.udpServerSocketAddress);
            else
                this.udpSendMsg(UDPChatChannel.UDP_ASCII_ART, this.udpServerSocketAddress);
        }
        else if(msg.startsWith(MulticastChatChannel.MULTICAST_MSG)){
            String msgContent = msg.substring(2);
            String parsedMsg = this.name + " (-M):" + msgContent;
            InetAddress address = InetAddress.getByName(MulticastChatChannel.MULTICAST_ADDRESS);
            InetSocketAddress multicastTarget = new InetSocketAddress(address, MulticastChatChannel.MULTICAST_PORT);
            if(msgContent.length() > 1)
                this.udpSendMsg(parsedMsg, multicastTarget);
            else
                this.udpSendMsg(UDPChatChannel.UDP_ASCII_ART, multicastTarget);
        }
        else{
            this.tcpSendMsg(msg);
        }
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

            Pair<SocketAddress, String> udpMsg = client.udpReceiveMsg();
            msg = udpMsg.getSecond();
            if(msg != null)
                System.out.println(msg);

            msg = client.multicastReceiveMsg();
            if(msg != null)
                System.out.println(msg);
            
            msg = client.stdIn.readLine();
            client.parseAndSendMsg(msg);
        }
    }
}
