package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Guarionex on 1/19/2016.
 */
public class DinningTable {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String number;
    @SerializedName("defaultSeats")
    private int seats;
    @SerializedName("type")
    private String style;
    @SerializedName("position")
    private Location location;
    private boolean wheelAccessibility;
    @SerializedName("dimensions")
    private Dimensions dimensions;

    public static DinningTable getDefaultDinningTable() {
        DinningTable table = new DinningTable();
        table.setNumber("1");
        table.setSeats(1);
        return table;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isWheelAccessibility() {
        return wheelAccessibility;
    }

    public void setWheelAccessibility(boolean wheelAccessibility) {
        this.wheelAccessibility = wheelAccessibility;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }
}
