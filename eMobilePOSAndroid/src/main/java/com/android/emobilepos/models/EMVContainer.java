package com.android.emobilepos.models;

import com.android.emobilepos.models.genius.GeniusResponse;
import com.handpoint.api.TransactionResult;

/**
 * Created by Guarionex on 12/22/2015.
 */
public class EMVContainer {
    private GeniusResponse geniusResponse;
    private TransactionResult handpointResponse;

    public EMVContainer(GeniusResponse geniusResponse) {
        this.geniusResponse = geniusResponse;
    }
    public EMVContainer(TransactionResult handpointResponse) {
        this.handpointResponse = handpointResponse;
    }
    public GeniusResponse getGeniusResponse() {
        return geniusResponse;
    }

    public void setGeniusResponse(GeniusResponse geniusResponse) {
        this.geniusResponse = geniusResponse;
    }

    public TransactionResult getHandpointResponse() {
        return handpointResponse;
    }

    public void setHandpointResponse(TransactionResult handpointResponse) {
        this.handpointResponse = handpointResponse;
    }
}
