package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class homeMenuConfig {
    @SerializedName("SalesReceipt")
    private boolean SalesReceipt;
    @SerializedName("Order")
    private boolean Order;
    @SerializedName("Return")
    private boolean Return;
    @SerializedName("Invoice")
    private boolean Invoice;
    @SerializedName("Estimate")
    private boolean Estimate;
    @SerializedName("Payment")
    private boolean Payment;
    @SerializedName("GiftCard")
    private boolean GiftCard;
    @SerializedName("LoyaltyCard")
    private boolean LoyaltyCard;
    @SerializedName("RewardCard")
    private boolean RewardCard;
    @SerializedName("Refund")
    private boolean Refund;
    @SerializedName("Route")
    private boolean Route;
    @SerializedName("Holds")
    private boolean Holds;
    @SerializedName("Consignment")
    private boolean Consignment;
    @SerializedName("InventoryTransfer")
    private boolean InventoryTransfer;
    @SerializedName("TipAdjustment")
    private boolean TipAdjustment;
    @SerializedName("Shifts")
    private boolean Shifts;
    @SerializedName("NoSale")
    private boolean NoSale;

    public boolean isSalesReceipt() {
        return SalesReceipt;
    }

    public void setSalesReceipt(boolean salesReceipt) {
        SalesReceipt = salesReceipt;
    }

    public boolean isOrder() {
        return Order;
    }

    public void setOrder(boolean order) {
        Order = order;
    }

    public boolean isReturn() {
        return Return;
    }

    public void setReturn(boolean aReturn) {
        Return = aReturn;
    }

    public boolean isInvoice() {
        return Invoice;
    }

    public void setInvoice(boolean invoice) {
        Invoice = invoice;
    }

    public boolean isEstimate() {
        return Estimate;
    }

    public void setEstimate(boolean estimate) {
        Estimate = estimate;
    }

    public boolean isPayment() {
        return Payment;
    }

    public void setPayment(boolean payment) {
        Payment = payment;
    }

    public boolean isGiftCard() {
        return GiftCard;
    }

    public void setGiftCard(boolean giftCard) {
        GiftCard = giftCard;
    }

    public boolean isLoyaltyCard() {
        return LoyaltyCard;
    }

    public void setLoyaltyCard(boolean loyaltyCard) {
        LoyaltyCard = loyaltyCard;
    }

    public boolean isRewardCard() {
        return RewardCard;
    }

    public void setRewardCard(boolean rewardCard) {
        RewardCard = rewardCard;
    }

    public boolean isRefund() {
        return Refund;
    }

    public void setRefund(boolean refund) {
        Refund = refund;
    }

    public boolean isRoute() {
        return Route;
    }

    public void setRoute(boolean route) {
        Route = route;
    }

    public boolean isHolds() {
        return Holds;
    }

    public void setHolds(boolean holds) {
        Holds = holds;
    }

    public boolean isConsignment() {
        return Consignment;
    }

    public void setConsignment(boolean consignment) {
        Consignment = consignment;
    }

    public boolean isInventoryTransfer() {
        return InventoryTransfer;
    }

    public void setInventoryTransfer(boolean inventoryTransfer) {
        InventoryTransfer = inventoryTransfer;
    }

    public boolean isTipAdjustment() {
        return TipAdjustment;
    }

    public void setTipAdjustment(boolean tipAdjustment) {
        TipAdjustment = tipAdjustment;
    }

    public boolean isShifts() {
        return Shifts;
    }

    public void setShifts(boolean shifts) {
        Shifts = shifts;
    }

    public boolean isNoSale() {
        return NoSale;
    }

    public void setNoSale(boolean noSale) {
        NoSale = noSale;
    }
}
