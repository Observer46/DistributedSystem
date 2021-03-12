package Chat;

import Chat.ChatChannels.TCPChatChannel;
import Chat.ChatChannels.UDPChatChannel;
import Chat.Utils.ClientData;
import Chat.Utils.Pair;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

    public static final String SERVER_KILL_COMMAND = "-kill";
    public static final String WRONG_NAME = "-wrong_name";
    public static final String ACCEPT_NAME = "-ok_name";
    public static final int SERVER_PORT = 6969;


    private final HashMap<String, ClientData> clientsMap = new HashMap<>();
    private final HashMap<SocketChannel, ClientData> tcpSocketsToClients = new HashMap<>();
    private final HashMap<SocketAddress, ClientData> udpSocketsToClients = new HashMap<>();
    private final ServerSocketChannel serverSocket;

    private final Selector tcpSelector;
    private final UDPChatChannel udpChannel;
    private boolean shutdown = false;


    public Server(int port) throws IOException {
        this.tcpSelector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", port));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(tcpSelector, SelectionKey.OP_ACCEPT);
        this.udpChannel = new UDPChatChannel(port);
    }

    public void addClientSocketOnly(ClientData clientData){ // Used during granting validation
        this.tcpSocketsToClients.put(clientData.getTcpSocketChannel(), clientData);
    }

    public void addClient(ClientData clientData) {  // When granted validation
        System.out.println("Added client: " + clientData.getName());
        this.clientsMap.put(clientData.getName(), clientData);
    }

    public String createClientList(){
        StringBuilder stringBuilder = new StringBuilder("Server: Clients in chat room: ");
        for(ClientData data : this.clientsMap.values())
            stringBuilder.append(data.getName() + ", ");
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    public boolean validateClient(String clientName){
        return !clientName.equals(Client.NO_NAME) && this.clientsMap.containsKey(clientName);
    }

    public void removeClient(String clientName) throws IOException {
        this.removeClient(clientName, false);
    }

    public void removeClient(String clientName, boolean shutdown) throws IOException {
        System.out.println("Removing client: " + clientName);
        this.clientsMap.get(clientName).cleanUp();
        this.clientsMap.remove(clientName);
        if(!shutdown)
            this.tcpSendMsgToClients("Server", clientName + " has disconnected.");
    }

    public void serverShutdown() throws IOException {
        Collection<ClientData> clients = new ArrayList<>(this.clientsMap.values());
        for (ClientData clientData : clients) {
            clientData.tcpSendMsg("Server: shutdown - connection lost.");
            removeClient(clientData.getName(), true);
        }
        this.tcpSelector.close();
    }

    public boolean endOfConnectionCheck(String clientName, String msg) throws IOException {
        if (msg.equals(Client.END_MSG)) {
            this.removeClient(clientName);
            return true;
        }
        if(msg.equals(SERVER_KILL_COMMAND)) {
            this.shutdown = true;
            return true;
        }
        return false;
    }

    public void acceptNewClient() throws IOException {
        TCPChatChannel clientChannel = new TCPChatChannel(this.tcpSelector, this.serverSocket);
        ClientData clientData = new ClientData(Client.NO_NAME, clientChannel);
        this.addClientSocketOnly(clientData);
        System.out.println("New connection: " + clientChannel.addressString());
        clientChannel.sendMsg("Enter your name:");
    }

    public void tcpProcessClients() throws IOException {
        this.tcpSelector.select();
        Set<SelectionKey> selectedKeys = this.tcpSelector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (key.isAcceptable()) {
                this.acceptNewClient();
            }
            if (key.isReadable()) {
                this.tcpProcessMsg(key);
            }
            iterator.remove();
        }
    }

    public void udpProcessClients() throws IOException {
        Pair<SocketAddress, String> datagramInfo = this.udpChannel.receiveMsg();
        while(datagramInfo.getFirst() != null){
            this.udpProcessMsg(datagramInfo);
            datagramInfo = this.udpChannel.receiveMsg();
        }
    }

    public void tcpLogClientMsg(String clientName, String msg){
        System.out.println("(TCP) Log: " + clientName + ": " + msg);
    }

    public void udpLogClientMsg(String clientName, String msg){
        System.out.println("(UDP) Log: " + clientName + ": " + msg);
    }

    public void tcpProcessMsg(SelectionKey key) throws IOException {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ClientData thisClient = this.tcpSocketsToClients.get(clientSocketChannel);

        String senderName = thisClient.getName();
        try {
            String msg = thisClient.tcpReceiveMsg();

            if (this.validateClient(senderName)) {
                if(msg.equals(Client.LIST_MSG))
                    this.clientsMap.get(senderName).tcpSendMsg(this.createClientList());
                else
                    this.tcpSendMsgToClients(senderName, msg);
            } else {  // Validation procedure
                if (this.clientsMap.containsKey(msg)) {
                    thisClient.tcpSendMsg(Server.WRONG_NAME);
                    thisClient.tcpSendMsg("Nickname: " + msg + " is already taken - pick new nickname:");
                } else if (msg.equals(Client.NO_NAME)) {
                    thisClient.tcpSendMsg(Server.WRONG_NAME);
                    thisClient.tcpSendMsg("Nickname: " + msg + " is not valid - pick valid nickname:");
                } else {
                    thisClient.tcpSendMsg(Server.ACCEPT_NAME);
                    thisClient.setName(msg);
                    this.tcpSendMsgToClients("Server: ", msg + " has joined the chat!");
                    this.addClient(thisClient);
                }
            }
        }catch (SocketException e){
            System.out.println("Connection to client: " + senderName +  " has been lost");
            this.removeClient(senderName);
        }
    }

    public void udpProcessMsg(Pair<SocketAddress, String> datagramInfo) throws IOException {
        String msg = datagramInfo.getSecond();
        if(msg.startsWith(UDPChatChannel.UDP_NAME_PREFIX)){
            String name = msg.substring(UDPChatChannel.UDP_NAME_PREFIX.length());
            this.addUdpChannelToClient(datagramInfo.getFirst(), name);
            return;
        }

        ClientData clientData = this.udpSocketsToClients.get(datagramInfo.getFirst());
        String clientName = clientData.getName();

        if(this.validateClient(clientName))
            this.udpSendMsgToClients(clientName, msg);
    }

    public void addUdpChannelToClient(SocketAddress address, String name) {
        ClientData clientData = this.clientsMap.get(name);
        clientData.setUdpAddressForServerSide(address);
        this.udpSocketsToClients.put(clientData.getUdpAddressServerSide(), clientData);
    }

    public void tcpSendMsgToClients(String senderName, String msg) throws IOException {
        if(!endOfConnectionCheck(senderName, msg)) {    // Don't send when message is ending connection
            for (ClientData receiver : this.clientsMap.values()) {
                if (!receiver.getName().equals(senderName))
                    receiver.tcpSendMsg(senderName + ": " + msg);
            }
        }
        this.tcpLogClientMsg(senderName, msg);
    }

    public void udpSendMsgToClients(String senderName, String msg) throws IOException {
        for (ClientData receiver : this.clientsMap.values()) {
            if (!receiver.getName().equals(senderName))
                this.udpChannel.sendMsg(senderName + " (-U): " + msg, receiver.getUdpAddressServerSide());
        }
        this.udpLogClientMsg(senderName, msg);
    }

    public void serverCycle() throws InterruptedException, IOException {
        while (true) {
            this.tcpProcessClients();
            this.udpProcessClients();
            if (shutdown) {
                this.serverShutdown();
                break;
            }
            Thread.sleep(10);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server is online");
        Server server = new Server(Server.SERVER_PORT);
        server.serverCycle();
    }
}
