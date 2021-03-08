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
    private MulticastChatChannel multicastChannel;
    private BufferedReader stdIn;
    private SocketAddress udpServerSocketAddress;

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

        this.tcpChannel.setNonBlocking();
        this.udpServerSocketAddress = new InetSocketAddress("localhost", Server.SERVER_PORT);
        String udpValidation = UDPChatChannel.UDP_NAME_PREFIX + this.name;
        this.udpSendMsg(udpValidation, this.udpServerSocketAddress);    // needs confirmation
        this.multicastChannel = new MulticastChatChannel();
    }

    public SocketAddress getUdpAddress() throws IOException {
        return this.udpChannel.getAddress();
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
            if(from == null)
                return "";

            String myAddressString = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress mySocketAddress = (InetSocketAddress) this.getUdpAddress();
            int myPort = mySocketAddress.getPort();
            SocketAddress myAddress = new InetSocketAddress(myAddressString, myPort);

            if(from.equals(myAddress))       // Do not send to yourself
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
            InetAddress address = InetAddress.getByName(MulticastChatChannel.MULTICAST_ADDRESS);
            InetSocketAddress multicastTarget = new InetSocketAddress(address, MulticastChatChannel.MULTICAST_PORT);

            if(msgContent.trim().length() == 0)
                msgContent = UDPChatChannel.UDP_ASCII_ART;

            String parsedMsg = this.name + " (-M):" + msgContent;
            this.udpSendMsg(parsedMsg, multicastTarget);
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
