package com.android.emobilepos.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import util.JsonUtils;

/**
 * Created by Guarionex on 1/19/2016.
 */
public class DinningTable extends RealmObject {
    private static final Gson GSON = JsonUtils.getInstance();

    @SerializedName("mesa_id")
    @PrimaryKey
    private String id;
    @SerializedName("loc_id")
    private String locationId;
    @SerializedName("mesa_desc")
    private String number;
    @SerializedName("mesa_seats")
    private int seats;
    @SerializedName("map_json")
    private String additionalInfoJson;
    @SerializedName("isactive")
    private boolean isActive;
    @SerializedName("isreadonly")
    private boolean isReaonly;
    @SerializedName("_update")
    private String lastUpdateDate;


    @SerializedName("type")
    private String style;
    @SerializedName("position")
    private Position position;
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

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getAdditionalInfoJson() {
        return additionalInfoJson;
    }

    public void setAdditionalInfoJson(String additionalInfoJson) {
        this.additionalInfoJson = additionalInfoJson;
    }

    public void parseAdditionalInfo() {
        Gson gson = JsonUtils.getInstance();
        DinningTable table = gson.fromJson(additionalInfoJson, DinningTable.class);
        this.dimensions = table.getDimensions();
        this.setPosition(table.getPosition());
        this.setStyle(table.getStyle());
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isReaonly() {
        return isReaonly;
    }

    public void setReaonly(boolean reaonly) {
        isReaonly = reaonly;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
