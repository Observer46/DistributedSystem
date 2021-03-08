package Chat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Test {

    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
        while(ni.hasMoreElements()){
            NetworkInterface n = ni.nextElement();
            System.out.println(n.getDisplayName() + ", " + n.getName());
            Enumeration<InetAddress> addresses = n.getInetAddresses();
            while(addresses.hasMoreElements()){
                InetAddress addr = addresses.nextElement();
                System.out.println("\t" + addr.toString());
            }
        }
    }
}
