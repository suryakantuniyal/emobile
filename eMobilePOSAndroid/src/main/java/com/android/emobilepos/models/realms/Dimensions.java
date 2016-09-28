package com.android.emobilepos.models.realms;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Guarionex on 4/6/2016.
 */
public class Dimensions  extends RealmObject {
    @SerializedName("height")
    private int height;
    @SerializedName("width")
    private int width;
    @SerializedName("radio")
    private int radio;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getRadio() {
        return radio;
    }

    public void setRadio(int radio) {
        this.radio = radio;
    }
}
