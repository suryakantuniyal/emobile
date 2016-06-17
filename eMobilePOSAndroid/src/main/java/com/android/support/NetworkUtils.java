package com.android.support;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Guarionex on 6/13/2016.
 */
public class NetworkUtils {

    public static boolean isConnectedToInternet(final Activity activity) {

        final ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean[] retVal = {false};
        synchronized (connManager) {
            if (connManager != null && connManager.getActiveNetworkInfo().isAvailable() && connManager.getActiveNetworkInfo().isConnected()) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        InetAddress inetAddress;
                        Socket socket = null;
                        try {
                            inetAddress = InetAddress.getByName("sync.enablermobile.com");
                            socket = new Socket();
                            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 443);
                            socket.connect(socketAddress, 5000);// try for 3 seconds
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (socket != null && socket.isConnected()) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        retVal[0] = true;
                        synchronized (connManager) {
                            connManager.notify();
                        }
                    }
                }).start();
                try {
                    connManager.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return retVal[0];
    }
}
