package com.android.emobilepos.models;

public class Orders {
    String _qty;
    String _name;
    String _value;
    String _discountAmount;
    String _taxAmount;
    String _taxQty;
    String _discountQty;
    String _total;
    String _prod_id;
    String _cat_id;
    String _attr_desc;
    String _discount_id;
    String _item_discount;

    String _ordprod_id;
    String _ordprod_comment;

    String _addon;
    String _isAdded;
    String _hasAddons;
    private int priceLoyaltyPoints;
    private int loyaltyPointBalance;

    String _overwritePrice;
    String _prodDescription;

    public Orders() {
        _qty = "";
        _name = "";
        _value = "";
        _discountAmount = "0.00";
        _taxAmount = "0.00";
        _taxQty = "0";
        _discountQty = "0";
        _addon = "0";
        _isAdded = "0";
        _hasAddons = "0";
        _total = "";
        _prod_id = "";
        _cat_id = "";
        _attr_desc = "";
        _ordprod_comment = "";

        _overwritePrice = "";
        _prodDescription = "";
        _ordprod_id = "";

    }

    public Orders(String qty, String name, String val, String distAmount, String taxAmount, String taxQty, String distQty, String total) {
        _qty = qty;
        _name = name;
        _value = val;
        _discountAmount = distAmount;
        _taxAmount = taxAmount;
        _taxQty = taxQty;
        _discountQty = distQty;
        _total = total;
    }

    public String getQty() {
        return this._qty;
    }

    public String getName() {
        return this._name;
    }

    public String getValue() {
        return this._value;
    }

    public String getDiscount() {
        return this._discountAmount;
    }

    public String getTax() {
        return this._taxAmount;
    }

    public String getTaxQty() {
        return this._taxQty;
    }

    public String getDistQty() {
        return this._discountQty;
    }

    public String getTotal() {

        return this._total;
    }

    public String getProdID() {
        return this._prod_id;
    }

    public String getAddon() {
        return this._addon;
    }

    public String getIsAdded() {
        return this._isAdded;
    }

    public String getHasAddon() {
        return this._hasAddons;
    }

    public String getCatID() {
        return this._cat_id;
    }

    public String getOrdprodID() {
        return this._ordprod_id;
    }

    public String getAttrDesc() {
        return this._attr_desc;
    }

    public String getOrderProdComment() {
        return this._ordprod_comment;
    }

    public String getDiscountID() {
        return this._discount_id;
    }

    public String getItemDiscount() {
        return this._item_discount;
    }

    public void setOrdProdComment(String val) {
        this._ordprod_comment = val;
    }

    public void setAttrDesc(String val) {
        this._attr_desc = val;
    }

    public void setOrdprodID(String val) {
        this._ordprod_id = val;
    }

    public void setCatID(String val) {
        this._cat_id = val;
    }

    public void setHasAddon(String val) {
        this._hasAddons = val;
    }

    public void setAddon(String val) {
        this._addon = val;
    }

    public void setIsAdded(String val) {
        this._isAdded = val;
    }

    public void setQty(String qty) {
        this._qty = qty;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public void setDiscount(String distAmount) {
        this._discountAmount = distAmount;
    }

    public void setTax(String taxAmount) {
        this._taxAmount = taxAmount;
    }

    public void setTaxQty(String taxQty) {
        this._taxQty = taxQty;
    }

    public void setDistQty(String distQty) {
        this._discountQty = distQty;
    }

    public void setTotal(String total) {

        this._total = total;
    }

    public void setDiscountID(String value) {
        this._discount_id = value;
    }

    public void setItemDiscount(String value) {
        this._item_discount = value;
    }

    public void setProdID(String prod_id) {
        this._prod_id = prod_id;
    }

    public void setOverwritePrice(String overwritePrice) {
        this._overwritePrice = overwritePrice;
    }

    public String getOverwritePrice() {
        return this._overwritePrice;
    }

    public void setProdDescription(String prodDescription) {
        this._prodDescription = prodDescription;
    }

    public String getProdDescription() {
        return this._prodDescription;
    }

    public int getPriceLoyaltyPoints() {
        return priceLoyaltyPoints;
    }

    public void setPriceLoyaltyPoints(int priceLoyaltyPoints) {
        this.priceLoyaltyPoints = priceLoyaltyPoints;
    }

    public int getLoyaltyPointBalance() {
        return loyaltyPointBalance;
    }

    public void setLoyaltyPointBalance(int loyaltyPointBalance) {
        this.loyaltyPointBalance = loyaltyPointBalance;
    }

    public boolean hasAddon() {
        return getHasAddon().equals("1") || getHasAddon().equalsIgnoreCase("true");
    }

    public boolean isAddon() {
        return getAddon().equals("1") || getAddon().equalsIgnoreCase("true");
    }

    public boolean isAdded() {
        return getIsAdded().equals("1") || getIsAdded().equalsIgnoreCase("true");
    }
}
