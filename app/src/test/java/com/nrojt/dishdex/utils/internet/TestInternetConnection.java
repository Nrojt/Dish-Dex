package com.nrojt.dishdex.utils.internet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TestInternetConnection extends InternetConnection{
    public static boolean isNetworkAvailable(){
        try {
            int timeout = 2000; //setting a timeout of 2 seconds
            Socket sock = new Socket();
            SocketAddress socketAddress = new InetSocketAddress("aefrwdsfdsfdfs", 53);

            sock.connect(socketAddress, timeout);
            sock.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
