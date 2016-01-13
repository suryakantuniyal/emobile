package com.android.emobilepos.models;

import com.android.emobilepos.models.genius.GeniusResponse;

/**
 * Created by Guarionex on 12/22/2015.
 */
public class EMVContainer {
    private GeniusResponse geniusResponse;

    public EMVContainer(GeniusResponse geniusResponse) {
        this.geniusResponse = geniusResponse;
    }

    public GeniusResponse getGeniusResponse() {
        return geniusResponse;
    }

    public void setGeniusResponse(GeniusResponse geniusResponse) {
        this.geniusResponse = geniusResponse;
    }
}
