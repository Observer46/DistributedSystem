package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class JavaTcpServer {

    public static void main(String[] args) throws IOException {

        System.out.println("JAVA TCP SERVER");
        int portNumber = 12346;

        try(ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            // create socket
            System.out.println("client connected");
            clientSocket.setSoTimeout(2500);

            while(true){

                // accept client


                // in & out streams



                // read msg, send response
                String msg = in.readLine();
                msg = msg == null ? "none" : msg;
                System.out.println("received msg: " + msg);
                out.println("Pong Java Tcp");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
