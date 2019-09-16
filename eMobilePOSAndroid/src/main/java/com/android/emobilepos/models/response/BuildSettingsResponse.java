package com.android.emobilepos.models.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BuildSettingsResponse {

    @SerializedName("buildSettings")
    private List<BuildSettings> buildSettings = new ArrayList<>();


    public List<BuildSettings> getBuildSettings() {
        return buildSettings;
    }

    public void setBuildSettings(List<BuildSettings> buildSettings) {
        this.buildSettings = buildSettings;
    }
}
