package com.android.emobilepos.models.xml;

/**
 * Created by Luis Camayd on 7/24/2018.
 */
public class EMSPayment {
    private int action = 0;
    private String appId = "";
    private String jobId = "";

    public EMSPayment() {
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}