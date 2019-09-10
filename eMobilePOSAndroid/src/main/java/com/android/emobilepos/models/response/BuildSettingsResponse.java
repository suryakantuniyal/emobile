package com.android.emobilepos.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BuildSettingsResponse {


    @Expose
    private List<BuildSettings> buildSettings;

    public List<BuildSettings> getBuildSettings() {
        return buildSettings;
    }

    public void setBuildSettings(List<BuildSettings> buildSettings) {
        this.buildSettings = buildSettings;
    }
}
