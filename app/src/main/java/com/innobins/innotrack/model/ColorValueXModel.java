package com.innobins.innotrack.model;

/**
 * Created by silence12 on 10/7/17.
 */

public class ColorValueXModel {

    public int color;
    public String valueName ;

    public ColorValueXModel(int color, String valueName) {
        this.color = color;
        this.valueName = valueName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
