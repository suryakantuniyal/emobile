package com.android.emobilepos.models.realms;

import io.realm.RealmObject;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class SyncServerConfiguration  {
    private String servername;
    private String ipAddress;
    private String port;

    public static SyncServerConfiguration getInstance(String message) {
        SyncServerConfiguration configuration = new SyncServerConfiguration();
        String[] split = message.split(":");
        if (split.length == 3) {
            configuration.setServername(split[0]);
            configuration.setIpAddress(split[1]);
            configuration.setPort(split[2]);
        }
        return configuration;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}