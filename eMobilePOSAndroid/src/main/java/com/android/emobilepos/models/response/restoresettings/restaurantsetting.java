package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class restaurantsetting {
    @SerializedName("Mode")
    private boolean Mode= false;
    @SerializedName("OrderOptions")
    private boolean OrderOptions= false;
    @SerializedName("TableSelection")
    private boolean TableSelection= false;
    @SerializedName("NumberOfSeats")
    private boolean NumberOfSeats= false;


    public boolean isMode() {
        return Mode;
    }

    public void setMode(boolean mode) {
        Mode = mode;
    }

    public boolean isOrderOptions() {
        return OrderOptions;
    }

    public void setOrderOptions(boolean orderOptions) {
        OrderOptions = orderOptions;
    }

    public boolean isTableSelection() {
        return TableSelection;
    }

    public void setTableSelection(boolean tableSelection) {
        TableSelection = tableSelection;
    }

    public boolean isNumberOfSeats() {
        return NumberOfSeats;
    }

    public void setNumberOfSeats(boolean numberOfSeats) {
        NumberOfSeats = numberOfSeats;
    }
}
