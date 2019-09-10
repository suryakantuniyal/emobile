package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class giftcardsetting {
    @SerializedName("ShowAlsoRedeem")
    private boolean ShowAlsoRedeem;
    @SerializedName("ShowRedeemAll")
    private boolean ShowRedeemAll;
    @SerializedName("UnitName")
    private String UnitName;
    @SerializedName("TupyxGift")
    private boolean TupyxGift;
    @SerializedName("AutoBalanceRequest")
    private boolean AutoBalanceRequest;
    @SerializedName("ShowBalanceAfterPayment")
    private boolean ShowBalanceAfterPayment;
    @SerializedName("UseStadisV4")
    private boolean UseStadisV4;

    public boolean isShowAlsoRedeem() {
        return ShowAlsoRedeem;
    }

    public void setShowAlsoRedeem(boolean showAlsoRedeem) {
        ShowAlsoRedeem = showAlsoRedeem;
    }

    public boolean isShowRedeemAll() {
        return ShowRedeemAll;
    }

    public void setShowRedeemAll(boolean showRedeemAll) {
        ShowRedeemAll = showRedeemAll;
    }

    public String getUnitName() {
        return UnitName;
    }

    public void setUnitName(String unitName) {
        UnitName = unitName;
    }

    public boolean isTupyxGift() {
        return TupyxGift;
    }

    public void setTupyxGift(boolean tupyxGift) {
        TupyxGift = tupyxGift;
    }

    public boolean isAutoBalanceRequest() {
        return AutoBalanceRequest;
    }

    public void setAutoBalanceRequest(boolean autoBalanceRequest) {
        AutoBalanceRequest = autoBalanceRequest;
    }

    public boolean isShowBalanceAfterPayment() {
        return ShowBalanceAfterPayment;
    }

    public void setShowBalanceAfterPayment(boolean showBalanceAfterPayment) {
        ShowBalanceAfterPayment = showBalanceAfterPayment;
    }

    public boolean isUseStadisV4() {
        return UseStadisV4;
    }

    public void setUseStadisV4(boolean useStadisV4) {
        UseStadisV4 = useStadisV4;
    }
}
