package com.android.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 6/13/2016.
 */
public class NetworkUtils {

    private static List<NetworkInfo> getConnectedNetworkInfo(ConnectivityManager connectivityManager) {
        NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
        List<NetworkInfo> connectedNetworks = new ArrayList<>();
        for (NetworkInfo networkInfo : allNetworkInfo) {
            if (networkInfo.isConnected()) {
                connectedNetworks.add(networkInfo);
            }
        }

        return connectedNetworks;
    }

    public static boolean isConnectedToInternet(final Context activity) {
        final boolean[] retVal = {false};
        try {
            final ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager == null)
                return false;
            synchronized (connManager) {
                if (!getConnectedNetworkInfo(connManager).isEmpty()) {

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
                                retVal[0] = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                retVal[0] = false;
                            } finally {
                                if (socket != null && socket.isConnected()) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            synchronized (connManager) {
                                connManager.notify();
                            }
                        }
                    }).start();
                    try {
                        connManager.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return retVal[0];
    }

    public static boolean isConnectedToLAN(final Context activity) {
        final boolean[] retVal = {false};
        try {
            final ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager == null)
                return false;
            if (!getConnectedNetworkInfo(connManager).isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return retVal[0];
    }
}
