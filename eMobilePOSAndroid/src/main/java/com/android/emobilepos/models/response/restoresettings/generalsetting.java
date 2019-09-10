package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class generalsetting {

    //GENERAL SETTINGS
    @SerializedName("AutoSync")
    private boolean autosync;
    @SerializedName("HoldsPollingService")
    private boolean HoldsPollingService;
    @SerializedName("TransNumPreffix")
    private String TransNumPreffix;
    @SerializedName("FastScanning")
    private boolean FastScanning;
    @SerializedName("SignatureRequired")
    private boolean SignatureRequired;
    @SerializedName("QRReadFromCam")
    private boolean QRReadFromCam;
    @SerializedName("MultipleCatPerProd")
    private boolean MultipleCatPerProd;
    @SerializedName("GroupTaxes")
    private boolean GroupTaxes;
    @SerializedName("RetailTaxes")
    private boolean RetailTaxes;
    @SerializedName("MixNMatch")
    private boolean MixNMatch;
    @SerializedName("ConfirmationScreen")
    private boolean ConfirmationScreen;
    @SerializedName("AskForComments")
    private boolean AskForComments;
    @SerializedName("SkipContactInfoPrompt")
    private boolean SkipContactInfoPrompt;
    @SerializedName("SkipAddMoreProducts")
    private boolean SkipAddMoreProducts;
    @SerializedName("RequireShift")
    private boolean RequireShift;
    @SerializedName("RestaurantModeShowScopeBar")
    private boolean RestaurantModeShowScopeBar;
    @SerializedName("UseClerks")
    private boolean UseClerks;
    @SerializedName("ClerkAutoLogOut")
    private boolean ClerkAutoLogOut;

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
