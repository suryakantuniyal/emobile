package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class restaurantsetting {
    @SerializedName("Mode")
    private boolean Mode;
    @SerializedName("OrderOptions")
    private boolean OrderOptions;
    @SerializedName("TableSelection")
    private boolean TableSelection;
    @SerializedName("NumberOfSeats")
    private boolean NumberOfSeats;


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
