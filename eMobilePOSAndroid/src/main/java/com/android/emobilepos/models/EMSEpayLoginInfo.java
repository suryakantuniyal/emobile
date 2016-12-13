package com.android.emobilepos.models;

import com.android.support.emsutils.EMSUtils;

/**
 * Created by guarionex on 12/12/16.
 */
public class EMSEpayLoginInfo {
    private String terminalId;
    private String secret;
    private EMSUtils.EPayStatusCode epayStatusCode;
    private String epayStatusMessasge;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public EMSUtils.EPayStatusCode getEpayStatusCode() {
        return epayStatusCode;
    }

    public void setEpayStatusCode(EMSUtils.EPayStatusCode epayStatusCode) {
        this.epayStatusCode = epayStatusCode;
    }

    public String getEpayStatusMessasge() {
        return epayStatusMessasge;
    }

    public void setEpayStatusMessasge(String epayStatusMessasge) {
        this.epayStatusMessasge = epayStatusMessasge;
    }
}
