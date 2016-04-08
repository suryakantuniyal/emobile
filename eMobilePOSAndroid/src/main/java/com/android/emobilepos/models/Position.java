package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Guarionex on 1/19/2016.
 */
public class Position {
    @SerializedName("x")
    private
    int positionX;
    @SerializedName("y")
    private
    int positionY;

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
}
