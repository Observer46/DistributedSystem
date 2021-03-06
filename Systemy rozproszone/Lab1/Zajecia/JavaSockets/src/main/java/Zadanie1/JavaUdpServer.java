package Zadanie1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class JavaUdpServer {

    public static void main(String args[])
    {
        System.out.println("JAVA UDP SERVER");
        DatagramSocket socket = null;
        int portNumber = 9008;

        try{
            socket = new DatagramSocket(portNumber);
            byte[] receiveBuffer = new byte[1024];

            while(true) {
                // receive msg from client
                Arrays.fill(receiveBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String msg = new String(receivePacket.getData());
                System.out.println("received msg: " + msg);

                // send reply to client (added)
                byte[] sendBuffer = "PONG".getBytes();
                DatagramPacket packetToClient = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                socket.send(packetToClient);
                System.out.println("Sent pong to client on: " + clientAddress.toString() + ":" + clientPort);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
