package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class OtherSettings {
    @SerializedName("homeMenuConfig")
    private HomeMenuConfig HomeMenuConfig = new HomeMenuConfig();
    @SerializedName("DefaultTransaction")
    private String DefaultTransaction="";
    @SerializedName("BlockPriceLevelChange")
    private boolean BlockPriceLevelChange= false;
    @SerializedName("RequireAddress")
    private boolean RequireAddress= false;
    @SerializedName("RequirePO")
    private boolean RequirePO= false;
    @SerializedName("SkipManagerPriceOverride")
    private boolean SkipManagerPriceOverride= false;
    @SerializedName("RequirePWToClockOut")
    private boolean RequirePWToClockOut= false;
    @SerializedName("MapsInsideApp")
    private boolean MapsInsideApp= false;
    @SerializedName("UseLocationInventory")
    private boolean UseLocationInventory= false;

    public HomeMenuConfig getHomeMenuConfig() {
        return HomeMenuConfig;
    }

    public void setHomeMenuConfig(HomeMenuConfig HomeMenuConfig) {
        this.HomeMenuConfig = HomeMenuConfig;
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

