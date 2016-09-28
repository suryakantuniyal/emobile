package com.android.emobilepos.models.realms;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Guarionex on 1/19/2016.
 */
public class Position  extends RealmObject {
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
