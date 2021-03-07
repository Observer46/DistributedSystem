package Chat;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

    public static final String SERVER_KILL_COMMAND = "-kill";
    public static final String WRONG_NAME = "-wrong_name";
    public static final String ACCEPT_NAME = "-ok_name";
    public static final int MAX_CLIENTS = 16;   // Not used (yet)
    public static final int SERVER_PORT = 6969;


    private final HashMap<String, Client> clientsMap = new HashMap<>();
    private final HashMap<SocketChannel, Client> tcpSocketsToClients = new HashMap<>();
    private final HashMap<SocketAddress, Client> udpSocketsToClients = new HashMap<>();
    private final ServerSocketChannel serverSocket;

    private final Selector tcpSelector;
    private final UDPChatChannel udpChannel;
    //private final ByteBuffer msgBuffer = ByteBuffer.allocate(UDPChatChannel.BUFFER_SIZE);  // For UDP

    private boolean shutdown = false;


    public Server(int port) throws IOException {
        this.tcpSelector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress("localhost", port));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(tcpSelector, SelectionKey.OP_ACCEPT);
        this.udpChannel = new UDPChatChannel(port);
    }

    public void addClientSocketOnly(Client client){ // Used during logging process
        this.tcpSocketsToClients.put(client.getTcpSocketChannel(), client);
    }

    private void addClient(Client client) throws IOException {  // When logging in process complete
        System.out.println("Added client: " + client.getName());
        this.clientsMap.put(client.getName(), client);
        //this.udpSocketsToClients.put(client.getUdpAddress(), client);
    }

    public boolean validateClient(String clientName){
        return !clientName.equals(Client.NO_NAME) && this.clientsMap.containsKey(clientName);
    }

    private void removeClient(String clientName) throws IOException {
        System.out.println("Removing client: " + clientName);
        this.clientsMap.get(clientName).cleanUp();
        this.clientsMap.remove(clientName);
    }

    private void serverShutdown() throws IOException {
        Collection<Client> clients = new ArrayList<>(this.clientsMap.values());
        for (Client client : clients) {
            client.tcpSendMsg("Server: shutdown - connection lost.");
            removeClient(client.getName());
        }
        this.tcpSelector.close();
    }

    private boolean endOfConnectionCheck(String clientName, String msg) throws IOException {
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

    private void acceptNewClient() throws IOException {
        TCPChatChannel clientChannel = new TCPChatChannel(this.tcpSelector, this.serverSocket);
        Client client = new Client(Client.NO_NAME, clientChannel);
        this.addClientSocketOnly(client);
        System.out.println("New connection: " + clientChannel.addressString());
        clientChannel.sendMsg("Enter your name:");
    }

    private void tcpProcessClients() throws IOException {
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

    private void tcpLogClientMsg(String clientName, String msg){
        System.out.println("(TCP) Log: " + clientName + ": " + msg);
    }

    public void udpLogClientMsg(String clientName, String msg){
        System.out.println("(UDP) Log: " + clientName + ": " + msg);
    }

    public void tcpProcessMsg(SelectionKey key) throws IOException {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        Client thisClient = this.tcpSocketsToClients.get(clientSocketChannel);

        String senderName = thisClient.getName();
        String msg = thisClient.tcpReceiveMsg();

        if(this.validateClient(senderName)){
            this.tcpSendMsgToClients(senderName, msg);
        }
        else {  // Validation procedure
            String clientName = msg;
            if(this.clientsMap.containsKey(clientName) ) {
                thisClient.tcpSendMsg(Server.WRONG_NAME);
                thisClient.tcpSendMsg("Nickname: " + clientName + " is already taken - pick new nickname:");
            }
            else if(clientName.equals(Client.NO_NAME)) {
                thisClient.tcpSendMsg(Server.WRONG_NAME);
                thisClient.tcpSendMsg("Nickname: " + clientName + " is not valid - pick valid nickname:");
            }
            else{
                thisClient.tcpSendMsg(Server.ACCEPT_NAME);
                thisClient.setName(clientName);
                this.addClient(thisClient);
            }
        }
    }

    public void udpProcessMsg(Pair<SocketAddress, String> datagramInfo) throws IOException {
        String msg = datagramInfo.getSecond();
        if(msg.startsWith(UDPChatChannel.UDP_NAME_PREFIX)){
            String name = msg.substring(UDPChatChannel.UDP_NAME_PREFIX.length());
            this.addUdpChannelToClient(datagramInfo.getFirst(), name);
            return;
        }

        Client client = this.udpSocketsToClients.get(datagramInfo.getFirst());
        String clientName = client.getName();
        if(this.validateClient(clientName))
            this.udpSendMsgToClients(clientName, msg);
    }

    private void addUdpChannelToClient(SocketAddress address, String name) {
        Client client = this.clientsMap.get(name);
        client.setUdpAddressForServerSide(address);
        this.udpSocketsToClients.put(client.getUdpAddressServerSide(), client);
    }

    public void tcpSendMsgToClients(String senderName, String msg) throws IOException {
        if(!endOfConnectionCheck(senderName, msg)) {    // Don't send when message is ending connection
            for (Client receiver : this.clientsMap.values()) {
                if (!receiver.getName().equals(senderName))
                    receiver.tcpSendMsg(senderName + ": " + msg);
            }
        }
        this.tcpLogClientMsg(senderName, msg);
    }

    public void udpSendMsgToClients(String senderName, String msg) throws IOException {
        for (Client receiver : this.clientsMap.values()) {
            if (!receiver.getName().equals(senderName))
                receiver.udpSendMsg(senderName + "(-U): " + msg, receiver.getUdpAddress());
        }
        this.udpLogClientMsg(senderName, msg);
    }

    private void serverCycle() throws InterruptedException, IOException {
        while (true) {
            this.tcpProcessClients();
            this.udpProcessClients();
            if(shutdown){
                this.serverShutdown();
                break;
            }
            Thread.sleep(10);
        }
    }

//    public void logBuffer(){
//        System.out.println("TCPBUFFER: " + new String(this.msgBuffer.array()).trim());
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server: Hello world!");
        Server server = new Server(Server.SERVER_PORT);
        server.serverCycle();
    }
}
