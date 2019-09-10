package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class productsettings {
    @SerializedName("AllowDecimalQuantities")
    private boolean AllowDecimalQuantities;
    @SerializedName("RemoveLeadZerosUPCSKU")
    private boolean RemoveLeadZerosUPCSKU;
    @SerializedName("GroupReceiptBySKU")
    private boolean GroupReceiptBySKU;
    @SerializedName("RequirePasswordRemoveVoid")
    private boolean RequirePasswordRemoveVoid;
    @SerializedName("ShowRemovedItemsOnPrintOut")
    private boolean ShowRemovedItemsOnPrintOut;
    @SerializedName("DefaultCategory")
    private String DefaultCategory;
    @SerializedName("AttributeToDisplay")
    private String AttributeToDisplay;
    @SerializedName("GroupInCatalogByName")
    private boolean GroupInCatalogByName;
    @SerializedName("FilterByCustomers")
    private boolean FilterByCustomers;
    @SerializedName("LimitProductsOnHand")
    private boolean LimitProductsOnHand;

    public boolean isAllowDecimalQuantities() {
        return AllowDecimalQuantities;
    }

    public void setAllowDecimalQuantities(boolean allowDecimalQuantities) {
        AllowDecimalQuantities = allowDecimalQuantities;
    }

    public boolean isRemoveLeadZerosUPCSKU() {
        return RemoveLeadZerosUPCSKU;
    }

    public void setRemoveLeadZerosUPCSKU(boolean removeLeadZerosUPCSKU) {
        RemoveLeadZerosUPCSKU = removeLeadZerosUPCSKU;
    }

    public boolean isGroupReceiptBySKU() {
        return GroupReceiptBySKU;
    }

    public void setGroupReceiptBySKU(boolean groupReceiptBySKU) {
        GroupReceiptBySKU = groupReceiptBySKU;
    }

    public boolean isRequirePasswordRemoveVoid() {
        return RequirePasswordRemoveVoid;
    }

    public void setRequirePasswordRemoveVoid(boolean requirePasswordRemoveVoid) {
        RequirePasswordRemoveVoid = requirePasswordRemoveVoid;
    }

    public boolean isShowRemovedItemsOnPrintOut() {
        return ShowRemovedItemsOnPrintOut;
    }

    public void setShowRemovedItemsOnPrintOut(boolean showRemovedItemsOnPrintOut) {
        ShowRemovedItemsOnPrintOut = showRemovedItemsOnPrintOut;
    }

    public String getDefaultCategory() {
        return DefaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        DefaultCategory = defaultCategory;
    }

    public String getAttributeToDisplay() {
        return AttributeToDisplay;
    }

    public void setAttributeToDisplay(String attributeToDisplay) {
        AttributeToDisplay = attributeToDisplay;
    }

    public boolean isGroupInCatalogByName() {
        return GroupInCatalogByName;
    }

    public void setGroupInCatalogByName(boolean groupInCatalogByName) {
        GroupInCatalogByName = groupInCatalogByName;
    }

    public boolean isFilterByCustomers() {
        return FilterByCustomers;
    }

    public void setFilterByCustomers(boolean filterByCustomers) {
        FilterByCustomers = filterByCustomers;
    }

    public boolean isLimitProductsOnHand() {
        return LimitProductsOnHand;
    }

    public void setLimitProductsOnHand(boolean limitProductsOnHand) {
        LimitProductsOnHand = limitProductsOnHand;
    }
}
