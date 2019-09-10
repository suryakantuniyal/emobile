package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class otherSettings {
    @SerializedName("homeMenuConfig")
    private homeMenuConfig homeMenuConfig;
    @SerializedName("DefaultTransaction")
    private String DefaultTransaction;
    @SerializedName("BlockPriceLevelChange")
    private boolean BlockPriceLevelChange;
    @SerializedName("RequireAddress")
    private boolean RequireAddress;
    @SerializedName("RequirePO")
    private boolean RequirePO;
    @SerializedName("SkipManagerPriceOverride")
    private boolean SkipManagerPriceOverride;
    @SerializedName("RequirePWToClockOut")
    private boolean RequirePWToClockOut;
    @SerializedName("MapsInsideApp")
    private boolean MapsInsideApp;
    @SerializedName("UseLocationInventory")
    private boolean UseLocationInventory;

    public homeMenuConfig getHomeMenuConfig() {
        return homeMenuConfig;
    }

    public void setHomeMenuConfig(homeMenuConfig homeMenuConfig) {
        this.homeMenuConfig = homeMenuConfig;
    }

    public String getDefaultTransaction() {
        return DefaultTransaction;
    }

    public void setDefaultTransaction(String defaultTransaction) {
        DefaultTransaction = defaultTransaction;
    }

    public boolean isBlockPriceLevelChange() {
        return BlockPriceLevelChange;
    }

    public void setBlockPriceLevelChange(boolean blockPriceLevelChange) {
        BlockPriceLevelChange = blockPriceLevelChange;
    }

    public boolean isRequireAddress() {
        return RequireAddress;
    }

    public void setRequireAddress(boolean requireAddress) {
        RequireAddress = requireAddress;
    }

    public boolean isRequirePO() {
        return RequirePO;
    }

    public void setRequirePO(boolean requirePO) {
        RequirePO = requirePO;
    }

    public boolean isSkipManagerPriceOverride() {
        return SkipManagerPriceOverride;
    }

    public void setSkipManagerPriceOverride(boolean skipManagerPriceOverride) {
        SkipManagerPriceOverride = skipManagerPriceOverride;
    }

    public boolean isRequirePWToClockOut() {
        return RequirePWToClockOut;
    }

    public void setRequirePWToClockOut(boolean requirePWToClockOut) {
        RequirePWToClockOut = requirePWToClockOut;
    }

    public boolean isMapsInsideApp() {
        return MapsInsideApp;
    }

    public void setMapsInsideApp(boolean mapsInsideApp) {
        MapsInsideApp = mapsInsideApp;
    }

    public boolean isUseLocationInventory() {
        return UseLocationInventory;
    }

    public void setUseLocationInventory(boolean useLocationInventory) {
        UseLocationInventory = useLocationInventory;
    }
}

