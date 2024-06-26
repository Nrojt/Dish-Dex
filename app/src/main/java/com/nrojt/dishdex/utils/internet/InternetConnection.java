package com.nrojt.dishdex.utils.internet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class InternetConnection {
    // Checks if the device is connected to the internet by pinging Google's DNS server.
    //This is better than checking if the device is connected to a WiFi network, because the device might be connected to a WiFi network but not have internet access.
    public static boolean isNetworkAvailable() {
        try {
            int timeout = 10000; //setting a timeout of 10 seconds
            Socket sock = new Socket();
            SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(socketAddress, timeout);
            sock.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
