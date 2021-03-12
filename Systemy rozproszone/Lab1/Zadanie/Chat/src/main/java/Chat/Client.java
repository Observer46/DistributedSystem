package Chat;

import Chat.ChatChannels.MulticastChatChannel;
import Chat.ChatChannels.TCPChatChannel;
import Chat.ChatChannels.UDPChatChannel;
import Chat.Utils.ChatTerminal;
import Chat.Utils.Pair;

import java.io.*;
import java.net.*;

public class Client {

    public static final String END_MSG = "-exit";
    public static final String HELP_MSG = "-help";
    public static final String NO_NAME = "unnamed";
    public static final String LIST_MSG = "-list";

    private String name;
    private final TCPChatChannel tcpChannel;
    private final UDPChatChannel udpChannel;
    private final MulticastChatChannel multicastChannel;
    private final ChatTerminal chatTerminal;
    private final SocketAddress udpServerSocketAddress;
    private boolean alive = true;

    public Client(InetAddress serverAddress, int serverPort) throws IOException {
        this.chatTerminal = new ChatTerminal();
        this.printToTerminal("Initiating connection to chat server...");
        this.udpChannel = new UDPChatChannel();
        this.tcpChannel = new TCPChatChannel(serverAddress, serverPort);
        this.printToTerminal("Connected to chat server!");

        this.name = Client.NO_NAME;

        while(this.name.equals(Client.NO_NAME)){
            this.printToTerminal(this.tcpChannel.receiveMsg());
            String name = this.chatTerminal.getInputBlocking();
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

    public boolean isAlive(){
        return this.alive;
    }

    public SocketAddress getUdpAddress() throws IOException {
        return this.udpChannel.getAddress();
    }

    public void tcpSendMsg(String msg) throws IOException {
        this.tcpChannel.sendMsg(msg);
        if(msg.equals(Client.END_MSG)) {
            this.cleanUp();
            this.alive = false;
        }
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
                return null;

            String myAddressString = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress mySocketAddress = (InetSocketAddress) this.getUdpAddress();
            int myPort = mySocketAddress.getPort();
            SocketAddress myAddress = new InetSocketAddress(myAddressString, myPort);

            if(from.equals(myAddress))       // Do not send to yourself
                return null;

            return datagramInfo.getSecond();
        }
        return null;
    }

    public Pair<SocketAddress, String> udpReceiveMsg() throws IOException {
        return this.udpChannel.receiveMsg();
    }

    public void cleanUp() throws IOException {
        this.tcpChannel.cleanUp();
        this.udpChannel.cleanUp();
        this.chatTerminal.close();
    }

    public void listCommands() throws IOException {
        this.printToTerminal(Client.HELP_MSG + " -> list all commands");
        this.printToTerminal(Client.END_MSG + " -> shut application down");
        this.printToTerminal(UDPChatChannel.UDP_MSG + " [message] -> send message using UDP\n(if no message specified, an ascii art cat is sent");
        this.printToTerminal(MulticastChatChannel.MULTICAST_MSG + " [message] -> send message using multicast UDP\n(if no message specified, an ascii art cat is sent");
        this.printToTerminal(Client.LIST_MSG + " -> list all clients in chat");
    }

    public void parseAndSendMsg(String msg) throws IOException {
        if(msg == null)
            return;
        if(msg.equals(Client.HELP_MSG)) {
            this.listCommands();
            return;
        }

        if(msg.startsWith(UDPChatChannel.UDP_MSG)){
            String msgContent = msg.substring(2);

            if(msgContent.trim().length() == 0)
                msgContent = UDPChatChannel.UDP_ASCII_ART;

            this.udpSendMsg(msgContent, this.udpServerSocketAddress);
            msg = this.name + " (-U): " + msgContent;
        }
        else if(msg.startsWith(MulticastChatChannel.MULTICAST_MSG)){
            String msgContent = msg.substring(2);
            InetAddress address = InetAddress.getByName(MulticastChatChannel.MULTICAST_ADDRESS);
            InetSocketAddress multicastTarget = new InetSocketAddress(address, MulticastChatChannel.MULTICAST_PORT);

            if(msgContent.trim().length() == 0)
                msgContent = UDPChatChannel.UDP_ASCII_ART;

            String parsedMsg = this.name + " (-M):" + msgContent;
            this.udpSendMsg(parsedMsg, multicastTarget);
            msg = parsedMsg;
        }
        else{
            this.tcpSendMsg(msg);
            msg = this.name + ": " + msg;
        }

        this.printToTerminal(msg);
    }

    public void printToTerminal(String text) throws IOException {
        this.chatTerminal.printBufferForClient(text);
    }

    public String getInputFromTerminal(){
        return this.chatTerminal.getStringToPrint();
    }

    public void updateTerminalInput() throws IOException {
        this.chatTerminal.putChar();
    }

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int port = Server.SERVER_PORT;
        try {
            Client client = new Client(serverAddress, port);
            client.printToTerminal("Successfully connected to chat server!");
            client.printToTerminal("For help with commands type -help");
            try {
                while (client.isAlive()) {
                    String msg = client.tcpReceiveMsg();
                    if (msg != null)
                        client.printToTerminal(msg);

                    Pair<SocketAddress, String> udpMsg = client.udpReceiveMsg();
                    msg = udpMsg.getSecond();
                    if (msg != null)
                        client.printToTerminal(msg);

                    msg = client.multicastReceiveMsg();
                    if (msg != null)
                        client.printToTerminal(msg);

                    client.updateTerminalInput();
                    msg = client.getInputFromTerminal();
                    client.parseAndSendMsg(msg);
                }
            }catch (SocketException e){
                System.out.println("Connection to server has been lost - shutting down!");
            }
            client.cleanUp();
        }catch(ConnectException e){
            System.out.println("Connection failed - cannot contact server");
        }
    }
}
