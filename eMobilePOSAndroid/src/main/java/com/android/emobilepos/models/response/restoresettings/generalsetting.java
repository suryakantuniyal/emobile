package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class generalsetting {

    //GENERAL SETTINGS
    @SerializedName("AutoSync")
    private boolean autosync = false;
    @SerializedName("HoldsPollingService")
    private boolean HoldsPollingService= false;
    @SerializedName("TransNumPreffix")
    private String TransNumPreffix = "";
    @SerializedName("FastScanning")
    private boolean FastScanning= false;
    @SerializedName("SignatureRequired")
    private boolean SignatureRequired= false;
    @SerializedName("QRReadFromCam")
    private boolean QRReadFromCam= false;
    @SerializedName("MultipleCatPerProd")
    private boolean MultipleCatPerProd= false;
    @SerializedName("GroupTaxes")
    private boolean GroupTaxes= false;
    @SerializedName("RetailTaxes")
    private boolean RetailTaxes= false;
    @SerializedName("MixNMatch")
    private boolean MixNMatch= false;
    @SerializedName("ConfirmationScreen")
    private boolean ConfirmationScreen= false;
    @SerializedName("AskForComments")
    private boolean AskForComments= false;
    @SerializedName("SkipContactInfoPrompt")
    private boolean SkipContactInfoPrompt= false;
    @SerializedName("SkipAddMoreProducts")
    private boolean SkipAddMoreProducts= false;
    @SerializedName("RequireShift")
    private boolean RequireShift= false;
    @SerializedName("RestaurantModeShowScopeBar")
    private boolean RestaurantModeShowScopeBar= false;
    @SerializedName("UseClerks")
    private boolean UseClerks= false;
    @SerializedName("ClerkAutoLogOut")
    private boolean ClerkAutoLogOut= false;

    public boolean isAutosync() {
        return autosync;
    }

    public void setAutosync(boolean autosync) {
        this.autosync = autosync;
    }

    public boolean isHoldsPollingService() {
        return HoldsPollingService;
    }

    public void setHoldsPollingService(boolean holdsPollingService) {
        HoldsPollingService = holdsPollingService;
    }

    public String getTransNumPreffix() {
        return TransNumPreffix;
    }

    public void setTransNumPreffix(String transNumPreffix) {
        TransNumPreffix = transNumPreffix;
    }

    public boolean isFastScanning() {
        return FastScanning;
    }

    public void setFastScanning(boolean fastScanning) {
        FastScanning = fastScanning;
    }

    public boolean isSignatureRequired() {
        return SignatureRequired;
    }

    public void setSignatureRequired(boolean signatureRequired) {
        SignatureRequired = signatureRequired;
    }

    public boolean isQRReadFromCam() {
        return QRReadFromCam;
    }

    public void setQRReadFromCam(boolean QRReadFromCam) {
        this.QRReadFromCam = QRReadFromCam;
    }

    public boolean isMultipleCatPerProd() {
        return MultipleCatPerProd;
    }

    public void setMultipleCatPerProd(boolean multipleCatPerProd) {
        MultipleCatPerProd = multipleCatPerProd;
    }

    public boolean isGroupTaxes() {
        return GroupTaxes;
    }

    public void setGroupTaxes(boolean groupTaxes) {
        GroupTaxes = groupTaxes;
    }

    public boolean isRetailTaxes() {
        return RetailTaxes;
    }

    public void setRetailTaxes(boolean retailTaxes) {
        RetailTaxes = retailTaxes;
    }

    public boolean isMixNMatch() {
        return MixNMatch;
    }

    public void setMixNMatch(boolean mixNMatch) {
        MixNMatch = mixNMatch;
    }

    public boolean isConfirmationScreen() {
        return ConfirmationScreen;
    }

    public void setConfirmationScreen(boolean confirmationScreen) {
        ConfirmationScreen = confirmationScreen;
    }

    public boolean isAskForComments() {
        return AskForComments;
    }

    public void setAskForComments(boolean askForComments) {
        AskForComments = askForComments;
    }

    public boolean isSkipContactInfoPrompt() {
        return SkipContactInfoPrompt;
    }

    public void setSkipContactInfoPrompt(boolean skipContactInfoPrompt) {
        SkipContactInfoPrompt = skipContactInfoPrompt;
    }

    public boolean isSkipAddMoreProducts() {
        return SkipAddMoreProducts;
    }

    public void setSkipAddMoreProducts(boolean skipAddMoreProducts) {
        SkipAddMoreProducts = skipAddMoreProducts;
    }

    public boolean isRequireShift() {
        return RequireShift;
    }

    public void setRequireShift(boolean requireShift) {
        RequireShift = requireShift;
    }

    public boolean isRestaurantModeShowScopeBar() {
        return RestaurantModeShowScopeBar;
    }

    public void setRestaurantModeShowScopeBar(boolean restaurantModeShowScopeBar) {
        RestaurantModeShowScopeBar = restaurantModeShowScopeBar;
    }

    public boolean isUseClerks() {
        return UseClerks;
    }

    public void setUseClerks(boolean useClerks) {
        UseClerks = useClerks;
    }

    public boolean isClerkAutoLogOut() {
        return ClerkAutoLogOut;
    }

    public void setClerkAutoLogOut(boolean clerkAutoLogOut) {
        ClerkAutoLogOut = clerkAutoLogOut;
    }

}
