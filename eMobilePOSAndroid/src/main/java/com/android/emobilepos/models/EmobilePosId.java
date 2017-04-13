package com.android.emobilepos.models;

/**
 * Created by guarionex on 2/22/17.
 */

public class EmobilePosId {
    private String deviceId;
    private String sequence;
    private String year;

    public EmobilePosId(String id) {
        String delims = "[\\-]";
        String[] tokens = id.split(delims);
        if (tokens.length == 3) {
            deviceId = tokens[0];
            sequence = tokens[1];
            year = tokens[2];
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
