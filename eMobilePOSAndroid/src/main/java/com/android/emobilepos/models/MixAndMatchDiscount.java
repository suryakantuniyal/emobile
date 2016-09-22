package com.android.emobilepos.models;

/**
 * Created by Guarionex on 5/23/2016.
 */
public class MixAndMatchDiscount {
    private int qty;
    private MixMatch mixMatch;

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public MixMatch getMixMatch() {
        return mixMatch;
    }

    public void setMixMatch(MixMatch mixMatch) {
        this.mixMatch = mixMatch;
    }
}
