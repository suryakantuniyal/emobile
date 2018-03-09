package com.android.emobilepos.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.android.emobilepos.models.realms.SyncServerConfiguration;
import com.android.support.MyPreferences;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
public class SyncConfigServerService extends Service {
    static String UDP_BROADCAST = "UDPBroadcast";
    String lastMessage;
    //Boolean shouldListenForUDPBroadcast = false;
    MulticastSocket socket;

    private void listenAndWaitAndThrowIntent(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[15000];
        WifiManager wifi;
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock;
        multicastLock = wifi.createMulticastLock("lock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
        if (socket == null || socket.isClosed()) {
            socket = new MulticastSocket(port);// new DatagramSocket(port, broadcastIP);
            socket.setBroadcast(true);
//            socket.joinGroup(InetAddress.);
        }

        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.e("UDP", "Waiting for UDP broadcast");
        socket.receive(packet);
        multicastLock.release();
        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();
        if (android.text.TextUtils.isEmpty(lastMessage) || !lastMessage.equalsIgnoreCase(message)) {
            SyncServerConfiguration serverConfiguration = SyncServerConfiguration.getInstance(message);
            MyPreferences preferences = new MyPreferences(this);
            preferences.setSyncPlusIPAddress(serverConfiguration.getIpAddress());
            preferences.setSyncPlusPort(serverConfiguration.getPort());
            lastMessage = message;
        }

        Log.e("UDP", "Got UDB broadcast from " + senderIP + ", message: " + message);

        broadcastIntent(senderIP, message);
        socket.close();
    }

    private void broadcastIntent(String senderIP, String message) {
        Intent intent = new Intent(SyncConfigServerService.UDP_BROADCAST);
        intent.putExtra("sender", senderIP);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    Thread UDPBroadcastThread;

    void startListenForUDPBroadcast() {
        UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress broadcastIP = InetAddress.getByName("224.0.0.1"); //172.16.238.42 //192.168.1.255
                    Integer port = 8001;
                    while (shouldRestartSocketListen) {
                        listenAndWaitAndThrowIntent(broadcastIP, port);
                    }
                    //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
                } catch (Exception e) {
                    Log.i("UDP", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                }
            }
        });
        UDPBroadcastThread.start();
    }

    private Boolean shouldRestartSocketListen = true;

    void stopListen() {
        shouldRestartSocketListen = false;
        socket.close();
    }

    @Override
    public void onCreate() {

    }


    @Override
    public void onDestroy() {
        stopListen();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.i("UDP", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService(Context context) {
        Intent service = new Intent(context, SyncConfigServerService.class);
        context.startService(service);
    }

    public static void stopService(Context context) {
        Intent stopIntent = new Intent(context, SyncConfigServerService.class);
        context.stopService(stopIntent);
    }

    public static String getUrl(String urlFormat, Context Context) {
        MyPreferences preferences = new MyPreferences(Context);
        return String.format(urlFormat, preferences.getSyncPlusIPAddress(), preferences.getSyncPlusPort());
    }
}
